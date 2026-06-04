package com.example.typing.service;

import com.example.typing.dto.BattleMessage;
import com.example.typing.dto.response.BattlePlayerResultResponse;
import com.example.typing.dto.response.BattleResultResponse;
import com.example.typing.entity.BattleResult;
import com.example.typing.entity.MagicWords;
import com.example.typing.entity.User;
import com.example.typing.repository.BattleResultRepository;
import com.example.typing.repository.MagicWordRepository;
import com.example.typing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BattleModeService {

    private final MagicWordRepository magicWordRepository;
    private final BattleResultRepository battleResultRepository;
    private final ActiveBattleService activeBattleService;
    private final UserRepository userRepository;

    public BattleModeService(
            MagicWordRepository magicWordRepository,
            BattleResultRepository battleResultRepository,
            ActiveBattleService activeBattleService,
            UserRepository userRepository) {
        this.magicWordRepository = magicWordRepository;
        this.battleResultRepository = battleResultRepository;
        this.activeBattleService = activeBattleService;
        this.userRepository = userRepository;
    }

    public List<MagicWords> getAllMagicWords() {
        return magicWordRepository.findAll();
    }

    /**
     * 両者が「たたかう」を選択した時点で対戦レコードを作成し、matchId（= 主キー）を返す
     */
    @Transactional
    public Long createMatch(Long player1Id, Long player2Id) {
        Long matchId = battleResultRepository.findTopByOrderByMatchIdDesc()
                .map(result -> result.getMatchId() + 1L)
                .orElse(1L);

        BattleResult match = new BattleResult();
        match.setMatchId(matchId);
        match.setPlayer1Id(player1Id);
        match.setPlayer2Id(player2Id);
        match.setPlayer1Hp(ActiveBattleService.INITIAL_HP);
        match.setPlayer2Hp(ActiveBattleService.INITIAL_HP);
        battleResultRepository.save(match);
        return matchId;
    }

    /**
     * START_BATTLE 通知失敗時など、未完了の対戦レコードを削除する
     */
    @Transactional
    public void cancelMatch(Long matchId) {
        if (matchId == null) {
            return;
        }
        battleResultRepository.findById(matchId).ifPresent(record -> {
            if (record.getFinishedAt() == null) {
                battleResultRepository.delete(record);
            }
        });
    }

    public boolean isParticipant(Long matchId, Long userId) {
        if (matchId == null || userId == null) {
            return false;
        }
        if (activeBattleService.isParticipant(matchId, userId)) {
            return true;
        }
        BattleResult record = loadBattleResult(matchId);
        return record != null && isParticipantRecord(record, userId);
    }

    /**
     * 対戦結果を1回だけ読み、存在確認と参加者チェックを行う（GET / POST 認可用）
     */
    public BattleResultAccess resolveBattleResultAccess(Long matchId, Long userId) {
        if (matchId == null || userId == null) {
            return BattleResultAccess.notFound();
        }
        BattleResult result = loadBattleResult(matchId);
        if (result == null) {
            return BattleResultAccess.notFound();
        }
        if (!canAccessBattleResult(result, matchId, userId)) {
            return BattleResultAccess.forbidden(result);
        }
        return BattleResultAccess.ok(result, buildBattleResultResponse(result));
    }

    /**
     * 対戦中の WS 更新を DB に反映する（本人 stats/HP + 与ダメージによる相手 HP 減少）
     */
    @Transactional
    public void recordBattleUpdate(Long matchId, Long userId, BattleMessage message) {
        if (matchId == null || userId == null || message == null) {
            return;
        }
        battleResultRepository.findById(matchId).ifPresent(record -> {
            if (record.getFinishedAt() != null) {
                return;
            }
            if (userId.equals(record.getPlayer1Id())) {
                applySenderUpdate(record, message, true);
                applyDamageToOpponent(record, message.getDamage(), true);
            } else if (userId.equals(record.getPlayer2Id())) {
                applySenderUpdate(record, message, false);
                applyDamageToOpponent(record, message.getDamage(), false);
            } else {
                return;
            }
            battleResultRepository.save(record);
        });
    }

    /**
     * 対戦終了時に結果を保存する（同一 matchId なら更新 = 1対戦1レコード）
     * 各プレイヤーは自分側の stats / HP のみ送信でき、winnerId はサーバー側で決定する
     */
    @Transactional
    public BattleResult saveBattleResult(BattleResult incoming, Long authenticatedUserId) {
        BattleResult existing = loadBattleResult(incoming.getMatchId());
        if (existing == null) {
            return null;
        }
        return saveBattleResult(incoming, authenticatedUserId, existing);
    }

    @Transactional
    public BattleResult saveBattleResult(
            BattleResult incoming,
            Long authenticatedUserId,
            BattleResult existing) {
        if (existing == null
                || incoming.getMatchId() == null
                || !incoming.getMatchId().equals(existing.getMatchId())) {
            return null;
        }
        if (!isParticipantRecord(existing, authenticatedUserId)) {
            throw new IllegalArgumentException("Not a participant of this match");
        }

        mergeOwnStats(existing, incoming, authenticatedUserId);
        mergeOwnHp(existing, incoming, authenticatedUserId);
        resolveWinnerFromHp(existing);

        if (hasBothPlayerResults(existing)) {
            existing.setFinishedAt(LocalDateTime.now());
            activeBattleService.unregisterBattle(existing.getMatchId());
            activeBattleService.clearHpSnapshot(existing.getMatchId());
        }

        return battleResultRepository.save(existing);
    }

    private boolean isParticipantRecord(BattleResult record, Long userId) {
        return userId != null
                && (userId.equals(record.getPlayer1Id()) || userId.equals(record.getPlayer2Id()));
    }

    private boolean canAccessBattleResult(BattleResult record, Long matchId, Long userId) {
        if (isParticipantRecord(record, userId)) {
            return true;
        }
        return activeBattleService.isParticipant(matchId, userId);
    }

    private BattleResult loadBattleResult(Long matchId) {
        if (matchId == null) {
            return null;
        }
        BattleResult result = battleResultRepository.findById(matchId).orElse(null);
        if (result != null) {
            resolveWinnerFromHp(result);
        }
        return result;
    }

    private BattleResultResponse buildBattleResultResponse(BattleResult result) {
        List<BattlePlayerResultResponse> players = new ArrayList<>();
        players.add(buildPlayerResult(
                result.getPlayer1Id(),
                "player1",
                result.getPlayer1Score(),
                result.getPlayer1AccuracyRate(),
                result.getPlayer1TypedChars(),
                result.getPlayer1MissCount(),
                result.getWinnerId() != null && result.getWinnerId().equals(result.getPlayer1Id())));
        players.add(buildPlayerResult(
                result.getPlayer2Id(),
                "player2",
                result.getPlayer2Score(),
                result.getPlayer2AccuracyRate(),
                result.getPlayer2TypedChars(),
                result.getPlayer2MissCount(),
                result.getWinnerId() != null && result.getWinnerId().equals(result.getPlayer2Id())));

        return BattleResultResponse.builder()
                .id(result.getMatchId())
                .winnerId(result.getWinnerId())
                .finishedAt(result.getFinishedAt() != null ? result.getFinishedAt().toString() : "")
                .players(players)
                .build();
    }

    private BattlePlayerResultResponse buildPlayerResult(
            Long userId,
            String role,
            Integer score,
            Integer accuracyRate,
            Integer typedChars,
            Integer missCount,
            boolean isWinner) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId).orElse(null);
        String name = user != null ? user.getName() : "Unknown";
        String iconImage = user != null ? user.getIconImage() : null;

        return BattlePlayerResultResponse.builder()
                .id(userId)
                .name(name)
                .iconImage(iconImage)
                .role(role)
                .score(score)
                .accuracyRate(accuracyRate)
                .typedChars(typedChars)
                .missCount(missCount)
                .isWinner(isWinner)
                .build();
    }

    private void applySenderUpdate(BattleResult record, BattleMessage message, boolean isPlayer1) {
        if (isPlayer1) {
            if (message.getCurrentHp() != null) {
                record.setPlayer1Hp(message.getCurrentHp());
            }
            if (message.getScore() != null) {
                record.setPlayer1Score(maxInt(record.getPlayer1Score(), message.getScore()));
            }
            if (message.getAccuracyRate() != null) {
                record.setPlayer1AccuracyRate(message.getAccuracyRate());
            }
            if (message.getTypedChars() != null) {
                record.setPlayer1TypedChars(maxInt(record.getPlayer1TypedChars(), message.getTypedChars()));
            }
            if (message.getMissCount() != null) {
                record.setPlayer1MissCount(maxInt(record.getPlayer1MissCount(), message.getMissCount()));
            }
            return;
        }

        if (message.getCurrentHp() != null) {
            record.setPlayer2Hp(message.getCurrentHp());
        }
        if (message.getScore() != null) {
            record.setPlayer2Score(maxInt(record.getPlayer2Score(), message.getScore()));
        }
        if (message.getAccuracyRate() != null) {
            record.setPlayer2AccuracyRate(message.getAccuracyRate());
        }
        if (message.getTypedChars() != null) {
            record.setPlayer2TypedChars(maxInt(record.getPlayer2TypedChars(), message.getTypedChars()));
        }
        if (message.getMissCount() != null) {
            record.setPlayer2MissCount(maxInt(record.getPlayer2MissCount(), message.getMissCount()));
        }
    }

    private void applyDamageToOpponent(BattleResult record, Integer damage, boolean attackerIsPlayer1) {
        if (damage == null || damage <= 0) {
            return;
        }
        if (attackerIsPlayer1) {
            int currentHp = record.getPlayer2Hp() != null ? record.getPlayer2Hp() : ActiveBattleService.INITIAL_HP;
            record.setPlayer2Hp(Math.max(0, currentHp - damage));
            return;
        }
        int currentHp = record.getPlayer1Hp() != null ? record.getPlayer1Hp() : ActiveBattleService.INITIAL_HP;
        record.setPlayer1Hp(Math.max(0, currentHp - damage));
    }

    private void mergeOwnStats(BattleResult existing, BattleResult incoming, Long authenticatedUserId) {
        if (authenticatedUserId.equals(existing.getPlayer1Id())) {
            existing.setPlayer1Score(maxInt(existing.getPlayer1Score(), incoming.getPlayer1Score()));
            existing.setPlayer1AccuracyRate(
                    maxInt(existing.getPlayer1AccuracyRate(), incoming.getPlayer1AccuracyRate()));
            existing.setPlayer1TypedChars(maxInt(existing.getPlayer1TypedChars(), incoming.getPlayer1TypedChars()));
            existing.setPlayer1MissCount(maxInt(existing.getPlayer1MissCount(), incoming.getPlayer1MissCount()));
            return;
        }
        if (authenticatedUserId.equals(existing.getPlayer2Id())) {
            existing.setPlayer2Score(maxInt(existing.getPlayer2Score(), incoming.getPlayer2Score()));
            existing.setPlayer2AccuracyRate(
                    maxInt(existing.getPlayer2AccuracyRate(), incoming.getPlayer2AccuracyRate()));
            existing.setPlayer2TypedChars(maxInt(existing.getPlayer2TypedChars(), incoming.getPlayer2TypedChars()));
            existing.setPlayer2MissCount(maxInt(existing.getPlayer2MissCount(), incoming.getPlayer2MissCount()));
        }
    }

    private void mergeOwnHp(BattleResult existing, BattleResult incoming, Long authenticatedUserId) {
        if (authenticatedUserId.equals(existing.getPlayer1Id()) && incoming.getPlayer1Hp() != null) {
            existing.setPlayer1Hp(incoming.getPlayer1Hp());
        }
        if (authenticatedUserId.equals(existing.getPlayer2Id()) && incoming.getPlayer2Hp() != null) {
            existing.setPlayer2Hp(incoming.getPlayer2Hp());
        }
    }

    private boolean hasBothPlayerResults(BattleResult record) {
        return record.getPlayer1Score() != null && record.getPlayer2Score() != null;
    }

    /**
     * HP で勝者を決定する。同 HP の場合は引き分け（winnerId = null）
     */
    private void resolveWinnerFromHp(BattleResult result) {
        Integer player1Hp = result.getPlayer1Hp();
        Integer player2Hp = result.getPlayer2Hp();
        if (player1Hp == null || player2Hp == null) {
            result.setWinnerId(null);
            return;
        }
        if (player1Hp > player2Hp) {
            result.setWinnerId(result.getPlayer1Id());
        } else if (player2Hp > player1Hp) {
            result.setWinnerId(result.getPlayer2Id());
        } else {
            result.setWinnerId(null);
        }
    }

    private Integer maxInt(Integer a, Integer b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return Math.max(a, b);
    }

    public BattleResult getBattleResult(Long matchId) {
        return loadBattleResult(matchId);
    }
}
