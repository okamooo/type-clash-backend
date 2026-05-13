package com.example.typing.service;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class MatchMakingService {

    private final Queue<Long> waitingPlayers = new ConcurrentLinkedQueue<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final AtomicLong matchIdCounter = new AtomicLong(1); // 1から始まる連番のID

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
        if (waitingPlayers.contains(userId)) {
            return; // すでにキューにいる場合は何もしない
        }

        waitingPlayers.add(userId);

        // 2人以上揃っている間、マッチングを行い続ける
        while (waitingPlayers.size() >= 2) {
            Long player1 = waitingPlayers.poll();
            Long player2 = waitingPlayers.poll();

            if (player1 != null && player2 != null) {
                // 対戦用の一意なID(Long)を生成
                Long matchId = matchIdCounter.getAndIncrement();

                // 各プレイヤーに通知を送る
                notifyPlayer(player1, matchId, player2);
                notifyPlayer(player2, matchId, player1);
            }
        }
    }

    /**
     * 特定のユーザーに対戦開始の通知を送る
     */
    private void notifyPlayer(Long userId, Long matchId, Long opponentId) {
        messagingTemplate.convertAndSend(
                "/topic/match/notification/" + userId,
                (Object) Map.of(
                        "matchId", matchId,
                        "opponentId", opponentId,
                        "status", "MATCHED"));
    }

    /**
     * ユーザーを待機列から削除する（キャンセル時など）
     */
    public void leaveQueue(long userId) {
        waitingPlayers.remove(userId);
    }
}
