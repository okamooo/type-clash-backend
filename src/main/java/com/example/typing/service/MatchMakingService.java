package com.example.typing.service;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MatchMakingService {

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
        System.out.println("DEBUG: [Service] User " + userId + " attempting to join queue.");
        if (waitingPlayers.contains(userId) || activeMatches.containsKey(userId)) {
            return;
        }

        waitingPlayers.add(userId);

        while (waitingPlayers.size() >= 2) {
            Long player1 = waitingPlayers.poll();
            Long player2 = waitingPlayers.poll();

            if (player1 != null && player2 != null) {
                System.out.println("DEBUG: [Service] Match found! " + player1 + " vs " + player2);

                activeMatches.put(player1, player2);
                activeMatches.put(player2, player1);
                pendingRoles.put(player1, "player1");
                pendingRoles.put(player2, "player2");

                notifyMatched(player1, player2, "player1");
                notifyMatched(player2, player1, "player2");
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
            System.out.println("DEBUG: [Service] User " + userId + " ready but not in active pair.");
            return;
        }

        String key = pairKey(userId, opponentId);
        readyPlayersPerPair.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(userId);
        System.out.println("DEBUG: [Service] Pair " + key + ": User " + userId + " is READY.");

        if (readyPlayersPerPair.get(key).size() < 2) {
            return;
        }

        Long player1Id = resolvePlayer1Id(userId, opponentId);
        Long player2Id = player1Id.equals(userId) ? opponentId : userId;

        Long matchId = battleModeService.createMatch(player1Id, player2Id);
        activeBattleService.registerBattle(matchId, player1Id, player2Id);
        long battleEndsAt = System.currentTimeMillis() + BATTLE_COUNTDOWN_MS + BATTLE_DURATION_MS;

        System.out.println("DEBUG: [Service] Pair " + key + ": Both READY. Starting match " + matchId);

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
     * ユーザーを待機列・ペアから削除する（キャンセル・離脱時）
     */
    public void leaveQueue(long userId) {
        System.out.println("DEBUG: [Service] User " + userId + " leave queue request.");

        waitingPlayers.remove(userId);

        Long opponentId = activeMatches.remove(userId);
        if (opponentId != null) {
            activeMatches.remove(opponentId);
            pendingRoles.remove(userId);
            pendingRoles.remove(opponentId);
            readyPlayersPerPair.remove(pairKey(userId, opponentId));

            System.out.println("DEBUG: [Service] Notifying opponent " + opponentId + " about cancellation.");

            messagingTemplate.convertAndSend(
                    "/topic/match/notification/" + opponentId,
                    (Object) Map.of("status", "CANCELLED"));

            joinQueue(opponentId);
        }
    }
}
