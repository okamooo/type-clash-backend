package com.example.typing.service;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MatchMakingService {

    private static final Logger log = LoggerFactory.getLogger(MatchMakingService.class);
    private static final long BATTLE_COUNTDOWN_MS = 3_000L;
    private static final long BATTLE_DURATION_MS = 60_000L;

    private final Queue<Long> waitingPlayers = new ConcurrentLinkedQueue<>();
    // ペア成立〜両者 ready まで (userId -> opponentId)
    private final Map<Long, Long> activeMatches = new ConcurrentHashMap<>();
    // マッチング時の role (userId -> "player1" | "player2")
    private final Map<Long, String> pendingRoles = new ConcurrentHashMap<>();
    // ペア単位の ready 状態 (pairKey -> Set of userIds)
    private final Map<String, java.util.Set<Long>> readyPlayersPerPair = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messagingTemplate;
    private final BattleModeService battleModeService;
    private final ActiveBattleService activeBattleService;

    public MatchMakingService(
            SimpMessagingTemplate messagingTemplate,
            BattleModeService battleModeService,
            ActiveBattleService activeBattleService) {
        this.messagingTemplate = messagingTemplate;
        this.battleModeService = battleModeService;
        this.activeBattleService = activeBattleService;
    }

    /**
     * ユーザーをマッチングキューに追加する。
     * 2人揃ったらペアを組む（matchId は両者 ready まで発行しない）。
     */
    public synchronized void joinQueue(long userId) {
        log.debug("User {} attempting to join queue.", userId);
        if (waitingPlayers.contains(userId)) {
            return;
        }
        if (activeMatches.containsKey(userId)) {
            log.warn("User {} is already in a match. Ignoring joinQueue request.", userId);
            return;
        }

        waitingPlayers.add(userId);

        while (waitingPlayers.size() >= 2) {
            Long player1 = waitingPlayers.poll();
            Long player2 = waitingPlayers.poll();

            if (player1 != null && player2 != null) {
                log.debug("Match found! {} vs {}", player1, player2);

                activeMatches.put(player1, player2);
                activeMatches.put(player2, player1);
                pendingRoles.put(player1, "player1");
                pendingRoles.put(player2, "player2");

                notifyMatched(player1, player2, "player1");
                notifyMatched(player2, player1, "player2");
            } else if (player1 != null) {
                waitingPlayers.add(player1);
                break;
            }
        }
    }

    private void notifyMatched(Long userId, Long opponentId, String role) {
        messagingTemplate.convertAndSend(
                "/topic/match/notification/" + userId,
                (Object) Map.of(
                        "opponentId", opponentId,
                        "status", "MATCHED",
                        "role", role));
    }

    /**
     * プレイヤーが「たたかう」を選択したことを報告する。
     * ペア内の両者が ready になった時点で matchId を発行し対戦開始する。
     */
    public synchronized void playerReady(long userId) {
        Long opponentId = activeMatches.get(userId);
        if (opponentId == null) {
            log.debug("User {} ready but not in active pair.", userId);
            return;
        }

        String key = pairKey(userId, opponentId);
        readyPlayersPerPair.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(userId);
        log.debug("Pair {}: User {} is READY.", key, userId);

        if (readyPlayersPerPair.get(key).size() < 2) {
            return;
        }

        Long player1Id = resolvePlayer1Id(userId, opponentId);
        Long player2Id = player1Id.equals(userId) ? opponentId : userId;

        Long matchId = battleModeService.createMatch(player1Id, player2Id);
        activeBattleService.registerBattle(matchId, player1Id, player2Id);
        long battleEndsAt = System.currentTimeMillis() + BATTLE_COUNTDOWN_MS + BATTLE_DURATION_MS;

        log.debug("Pair {}: Both READY. Starting match {}", key, matchId);

        notifyStartBattle(userId, matchId, battleEndsAt);
        notifyStartBattle(opponentId, matchId, battleEndsAt);

        readyPlayersPerPair.remove(key);
        pendingRoles.remove(userId);
        pendingRoles.remove(opponentId);
        activeMatches.remove(userId);
        activeMatches.remove(opponentId);
    }

    private void notifyStartBattle(Long userId, Long matchId, long battleEndsAt) {
        messagingTemplate.convertAndSend(
                "/topic/match/notification/" + userId,
                (Object) Map.of(
                        "status", "START_BATTLE",
                        "matchId", matchId,
                        "battleEndsAt", battleEndsAt));
    }

    private Long resolvePlayer1Id(long userId, long opponentId) {
        if ("player1".equals(pendingRoles.get(userId))) {
            return userId;
        }
        if ("player1".equals(pendingRoles.get(opponentId))) {
            return opponentId;
        }
        return Math.min(userId, opponentId);
    }

    private String pairKey(long userId1, long userId2) {
        long min = Math.min(userId1, userId2);
        long max = Math.max(userId1, userId2);
        return min + "-" + max;
    }

    /**
     * 待機列からの離脱のみ（activeMatches は触らない）
     */
    public synchronized void leaveQueue(long userId) {
        log.debug("User {} leave queue request.", userId);
        waitingPlayers.remove(userId);
    }

    /**
     * 成立済み対戦（マッチング〜ready 前）からの離脱
     */
    public synchronized void leaveMatch(long userId) {
        log.debug("User {} leave match request.", userId);
        Long opponentId = activeMatches.remove(userId);
        if (opponentId != null) {
            activeMatches.remove(opponentId);
            pendingRoles.remove(userId);
            pendingRoles.remove(opponentId);
            readyPlayersPerPair.remove(pairKey(userId, opponentId));

            log.debug("Notifying opponent {} about opponent left.", opponentId);

            messagingTemplate.convertAndSend(
                    "/topic/match/notification/" + opponentId,
                    (Object) Map.of("status", "OPPONENT_LEFT"));

            joinQueue(opponentId);
        }
    }
}
