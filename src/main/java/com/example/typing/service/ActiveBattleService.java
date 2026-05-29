package com.example.typing.service;

import com.example.typing.dto.BattleMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActiveBattleService {

    public static final int INITIAL_HP = 100;

    public record HpSnapshot(Integer player1Hp, Integer player2Hp) {
    }

    private static final class BattleState {
        private final Long matchId;
        private final Long player1Id;
        private final Long player2Id;
        private Integer player1Hp;
        private Integer player2Hp;

        private BattleState(Long matchId, Long player1Id, Long player2Id) {
            this.matchId = matchId;
            this.player1Id = player1Id;
            this.player2Id = player2Id;
            this.player1Hp = INITIAL_HP;
            this.player2Hp = INITIAL_HP;
        }

        private HpSnapshot toSnapshot() {
            return new HpSnapshot(player1Hp, player2Hp);
        }
    }

    private final Map<Long, BattleState> battlesByMatchId = new ConcurrentHashMap<>();
    private final Map<Long, Long> matchIdByUserId = new ConcurrentHashMap<>();
    private final Map<Long, HpSnapshot> hpByMatchId = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final BattleModeService battleModeService;

    public ActiveBattleService(
            SimpMessagingTemplate messagingTemplate,
            @Lazy BattleModeService battleModeService) {
        this.messagingTemplate = messagingTemplate;
        this.battleModeService = battleModeService;
    }

    public void registerBattle(Long matchId, Long player1Id, Long player2Id) {
        BattleState battle = new BattleState(matchId, player1Id, player2Id);
        battlesByMatchId.put(matchId, battle);
        matchIdByUserId.put(player1Id, matchId);
        matchIdByUserId.put(player2Id, matchId);
        hpByMatchId.put(matchId, battle.toSnapshot());
    }

    public void unregisterBattle(Long matchId) {
        if (matchId == null) {
            return;
        }
        BattleState battle = battlesByMatchId.remove(matchId);
        if (battle != null) {
            matchIdByUserId.remove(battle.player1Id);
            matchIdByUserId.remove(battle.player2Id);
        }
    }

    public void clearHpSnapshot(Long matchId) {
        if (matchId != null) {
            hpByMatchId.remove(matchId);
        }
    }

    public boolean isParticipant(Long matchId, Long userId) {
        if (matchId == null || userId == null) {
            return false;
        }
        BattleState battle = battlesByMatchId.get(matchId);
        if (battle == null) {
            return false;
        }
        return userId.equals(battle.player1Id) || userId.equals(battle.player2Id);
    }

    public void updatePlayerHp(Long matchId, Long userId, Integer currentHp) {
        if (matchId == null || userId == null || currentHp == null) {
            return;
        }
        BattleState battle = battlesByMatchId.get(matchId);
        if (battle == null) {
            return;
        }
        if (userId.equals(battle.player1Id)) {
            battle.player1Hp = currentHp;
        } else if (userId.equals(battle.player2Id)) {
            battle.player2Hp = currentHp;
        } else {
            return;
        }
        hpByMatchId.put(matchId, battle.toSnapshot());
    }

    public void syncBattleUpdate(Long matchId, Long userId, BattleMessage message) {
        if (matchId == null || userId == null || message == null) {
            return;
        }
        BattleState battle = battlesByMatchId.get(matchId);
        if (battle == null) {
            return;
        }
        if (userId.equals(battle.player1Id)) {
            if (message.getCurrentHp() != null) {
                battle.player1Hp = message.getCurrentHp();
            }
            applyDamageToOpponent(battle, message.getDamage(), true);
        } else if (userId.equals(battle.player2Id)) {
            if (message.getCurrentHp() != null) {
                battle.player2Hp = message.getCurrentHp();
            }
            applyDamageToOpponent(battle, message.getDamage(), false);
        } else {
            return;
        }
        hpByMatchId.put(matchId, battle.toSnapshot());
    }

    private void applyDamageToOpponent(BattleState battle, Integer damage, boolean attackerIsPlayer1) {
        if (damage == null || damage <= 0) {
            return;
        }
        if (attackerIsPlayer1) {
            battle.player2Hp = Math.max(0, battle.player2Hp - damage);
        } else {
            battle.player1Hp = Math.max(0, battle.player1Hp - damage);
        }
    }

    public HpSnapshot getHpSnapshot(Long matchId) {
        BattleState battle = battlesByMatchId.get(matchId);
        if (battle != null) {
            return battle.toSnapshot();
        }
        return hpByMatchId.get(matchId);
    }

    /**
     * 対戦中の離脱（forfeit）を処理し、残ったプレイヤーに不戦勝を通知する
     */
    public synchronized void forfeit(long userId, Long matchIdHint) {
        Long matchId = matchIdHint != null ? matchIdHint : matchIdByUserId.get(userId);
        if (matchId == null) {
            return;
        }

        BattleState battle = battlesByMatchId.remove(matchId);
        if (battle == null) {
            return;
        }

        matchIdByUserId.remove(battle.player1Id);
        matchIdByUserId.remove(battle.player2Id);

        if (battle.player1Id == userId) {
            battle.player1Hp = 0;
            hpByMatchId.put(matchId, battle.toSnapshot());
            battleModeService.recordBattleUpdate(matchId, userId, forfeitMessage(userId, matchId, 0));
            notifyOpponentLeft(battle.player2Id, matchId, battle.player2Id);
        } else if (battle.player2Id == userId) {
            battle.player2Hp = 0;
            hpByMatchId.put(matchId, battle.toSnapshot());
            battleModeService.recordBattleUpdate(matchId, userId, forfeitMessage(userId, matchId, 0));
            notifyOpponentLeft(battle.player1Id, matchId, battle.player1Id);
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

    private BattleMessage forfeitMessage(Long userId, Long matchId, int currentHp) {
        BattleMessage message = new BattleMessage();
        message.setUserId(userId);
        message.setMatchId(matchId);
        message.setCurrentHp(currentHp);
        message.setContent("LOSER");
        return message;
    }
}
