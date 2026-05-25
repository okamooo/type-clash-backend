package com.example.typing.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActiveBattleService {

    private record ActiveBattle(Long matchId, Long player1Id, Long player2Id) {
    }

    private final Map<Long, ActiveBattle> battlesByMatchId = new ConcurrentHashMap<>();
    private final Map<Long, Long> matchIdByUserId = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public ActiveBattleService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void registerBattle(Long matchId, Long player1Id, Long player2Id) {
        ActiveBattle battle = new ActiveBattle(matchId, player1Id, player2Id);
        battlesByMatchId.put(matchId, battle);
        matchIdByUserId.put(player1Id, matchId);
        matchIdByUserId.put(player2Id, matchId);
    }

    public void unregisterBattle(Long matchId) {
        if (matchId == null) {
            return;
        }
        ActiveBattle battle = battlesByMatchId.remove(matchId);
        if (battle != null) {
            matchIdByUserId.remove(battle.player1Id());
            matchIdByUserId.remove(battle.player2Id());
        }
    }

    /**
     * 対戦中の離脱（forfeit）を処理し、残ったプレイヤーに不戦勝を通知する
     */
    public synchronized void forfeit(long userId, Long matchIdHint) {
        Long matchId = matchIdHint != null ? matchIdHint : matchIdByUserId.get(userId);
        if (matchId == null) {
            return;
        }

        ActiveBattle battle = battlesByMatchId.remove(matchId);
        if (battle == null) {
            return;
        }

        matchIdByUserId.remove(battle.player1Id());
        matchIdByUserId.remove(battle.player2Id());

        if (battle.player1Id() == userId) {
            notifyOpponentLeft(battle.player2Id(), matchId, battle.player2Id());
        } else if (battle.player2Id() == userId) {
            notifyOpponentLeft(battle.player1Id(), matchId, battle.player1Id());
        }
    }

    public void handlePlayerDisconnect(long userId) {
        forfeit(userId, null);
    }

    private void notifyOpponentLeft(Long recipientUserId, Long matchId, Long winnerId) {
        System.out.println("DEBUG: [ActiveBattle] Opponent left match " + matchId + ". Winner: " + winnerId);
        messagingTemplate.convertAndSend(
                "/topic/match/notification/" + recipientUserId,
                (Object) Map.of(
                        "status", "OPPONENT_LEFT",
                        "matchId", matchId,
                        "winnerId", winnerId));
    }
}
