package com.example.typing.service;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MatchMakingService {

    private static final Logger log = LoggerFactory.getLogger(MatchMakingService.class);

    private final Queue<Long> waitingPlayers = new ConcurrentLinkedQueue<>();
    // 現在対戦中のペアを保持 (userId -> opponentId)
    private final Map<Long, Long> activeMatches = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messagingTemplate;

    public MatchMakingService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * ユーザーをマッチングキューに追加する
     * 2人揃ったら対戦IDを生成し、それぞれのユーザーに通知する
     *
     * @param userId マッチングを希望するユーザーのID
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
                createMatch(player1, player2);
            } else if (player1 != null) {
                waitingPlayers.add(player1);
                break;
            }
        }
    }

    private void createMatch(Long player1, Long player2) {
        log.debug("Match found! {} vs {}", player1, player2);
        String matchId = UUID.randomUUID().toString();

        try {
            notifyPlayer(player1, matchId, player2);
            notifyPlayer(player2, matchId, player1);
            activeMatches.put(player1, player2);
            activeMatches.put(player2, player1);
        } catch (Exception e) {
            log.error("Failed to notify players. Requeueing both.", e);
            joinQueue(player1);
            joinQueue(player2);
        }
    }

    private void notifyPlayer(Long userId, String matchId, Long opponentId) {
        messagingTemplate.convertAndSend(
                "/topic/match/notification/" + userId,
                (Object) Map.of(
                        "matchId", matchId,
                        "opponentId", opponentId,
                        "status", "MATCHED"));
    }

    /**
     * 待機列からの離脱のみ（activeMatches は触らない）
     */
    public synchronized void leaveQueue(long userId) {
        log.debug("User {} leave queue request.", userId);
        waitingPlayers.remove(userId);
    }

    /**
     * 成立済み対戦からの離脱
     */
    public synchronized void leaveMatch(long userId) {
        log.debug("User {} leave match request.", userId);
        Long opponentId = activeMatches.remove(userId);
        if (opponentId != null) {
            activeMatches.remove(opponentId);

            log.debug("Notifying opponent {} about opponent left.", opponentId);

            messagingTemplate.convertAndSend(
                    "/topic/match/notification/" + opponentId,
                    (Object) Map.of("status", "OPPONENT_LEFT"));

            joinQueue(opponentId);
        }
    }
}
