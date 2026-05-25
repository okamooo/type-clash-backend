package com.example.typing.service;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MatchMakingService {

    private final Queue<Long> waitingPlayers = new ConcurrentLinkedQueue<>();
    // 現在対戦中のペアを保持 (userId -> opponentId)
    private final Map<Long, Long> activeMatches = new ConcurrentHashMap<>();
    // マッチごとの準備完了プレイヤーを保持 (matchId -> Set of userIds)
    private final Map<Long, java.util.Set<Long>> readyPlayersPerMatch = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messagingTemplate;
    private final BattleModeService battleModeService;

    public MatchMakingService(SimpMessagingTemplate messagingTemplate, BattleModeService battleModeService) {
        this.messagingTemplate = messagingTemplate;
        this.battleModeService = battleModeService;
    }

    /**
     * ユーザーをマッチングキューに追加する
     * 2人揃ったら対戦IDを生成し、それぞれのユーザーに通知する
     * 
     * @param userId マッチングを希望するユーザーのID
     */
    public synchronized void joinQueue(long userId) {
        System.out.println("DEBUG: [Service] User " + userId + " attempting to join queue.");
        if (waitingPlayers.contains(userId)) {
            return;
        }

        waitingPlayers.add(userId);

        // 2人以上揃っている間、マッチングを行い続ける
        while (waitingPlayers.size() >= 2) {
            Long player1 = waitingPlayers.poll();
            Long player2 = waitingPlayers.poll();

            if (player1 != null && player2 != null) {
                System.out.println("DEBUG: [Service] Match found! " + player1 + " vs " + player2);
                
                // 対戦ペアを記録
                activeMatches.put(player1, player2);
                activeMatches.put(player2, player1);

                // 対戦用の一意なID（DBの主キー）を生成
                Long matchId = battleModeService.createMatch(player1, player2);

                // 各プレイヤーに通知を送る
                notifyPlayer(player1, matchId, player2, "player1");
                notifyPlayer(player2, matchId, player1, "player2");
            }
        }
    }

    /**
     * 特定のユーザーに対戦開始の通知を送る
     */
    private void notifyPlayer(Long userId, Long matchId, Long opponentId, String role) {
        messagingTemplate.convertAndSend(
                "/topic/match/notification/" + userId,
                (Object) Map.of(
                        "matchId", matchId,
                        "opponentId", opponentId,
                        "status", "MATCHED",
                        "role", role));
    }

    /**
     * プレイヤーが準備完了したことを報告する
     */
    public synchronized void playerReady(Long matchId, Long userId) {
        System.out.println("DEBUG: [Service] Match " + matchId + ": User " + userId + " is READY.");
        
        readyPlayersPerMatch.computeIfAbsent(matchId, k -> ConcurrentHashMap.newKeySet()).add(userId);

        // 2人揃ったら対戦開始の合図を送る
        if (readyPlayersPerMatch.get(matchId).size() >= 2) {
            System.out.println("DEBUG: [Service] Match " + matchId + ": Both players READY. Starting countdown!");
            
            // チャンネルは /topic/battle/{matchId} を使用
            messagingTemplate.convertAndSend(
                "/topic/battle/" + matchId,
                (Object) Map.of("status", "START_BATTLE")
            );

            // 対戦開始後は leave を「逃げ」扱いしない（結果画面遷移時の queue/leave 対策）
            java.util.Set<Long> readyPlayers = readyPlayersPerMatch.remove(matchId);
            if (readyPlayers != null) {
                for (Long uid : readyPlayers) {
                    activeMatches.remove(uid);
                }
            }
        }
    }

    /**
     * ユーザーを待機列から削除する（キャンセル時など）
     */
    public void leaveQueue(long userId) {
        System.out.println("DEBUG: [Service] User " + userId + " leave queue request.");
        
        // 1. 待機列から削除
        waitingPlayers.remove(userId);

        // 2. 対戦中の相手がいる場合は通知を送る
        Long opponentId = activeMatches.remove(userId);
        if (opponentId != null) {
            activeMatches.remove(opponentId); // 相手側のデータも削除
            
            System.out.println("DEBUG: [Service] Notifying opponent " + opponentId + " about cancellation.");
            
            // 相手にキャンセルを通知
            messagingTemplate.convertAndSend(
                "/topic/match/notification/" + opponentId,
                (Object) Map.of("status", "CANCELLED")
            );

            // 相手を再び待機列に戻してあげる
            joinQueue(opponentId);
        }
    }
}
