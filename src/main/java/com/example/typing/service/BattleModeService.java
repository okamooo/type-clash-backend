package com.example.typing.service;

import com.example.typing.entity.BattleResult;
import com.example.typing.entity.MagicWords;
import com.example.typing.repository.BattleResultRepository;
import com.example.typing.repository.MagicWordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BattleModeService {

    private final MagicWordRepository magicWordRepository;
    private final BattleResultRepository battleResultRepository;
    private final ActiveBattleService activeBattleService;

    public BattleModeService(
            MagicWordRepository magicWordRepository,
            BattleResultRepository battleResultRepository,
            ActiveBattleService activeBattleService) {
        this.magicWordRepository = magicWordRepository;
        this.battleResultRepository = battleResultRepository;
        this.activeBattleService = activeBattleService;
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

    /**
     * 対戦終了時に結果を保存する（同一 matchId なら更新 = 1対戦1レコード）
     * 両プレイヤーが POST するため、数値は max でマージし winnerId はサーバー側で決定する
     */
    @Transactional
    public BattleResult saveBattleResult(BattleResult result) {
        BattleResult existing = battleResultRepository.findById(result.getMatchId()).orElse(null);

        if (existing != null) {
            Long preservedWinnerId = existing.getWinnerId();
            mergeStats(existing, result);
            resolveWinnerFromScores(existing);
            if (existing.getWinnerId() == null) {
                if (result.getWinnerId() != null) {
                    existing.setWinnerId(result.getWinnerId());
                } else if (preservedWinnerId != null) {
                    existing.setWinnerId(preservedWinnerId);
                }
            }
            existing.setFinishedAt(LocalDateTime.now());
            BattleResult saved = battleResultRepository.save(existing);
            activeBattleService.unregisterBattle(saved.getMatchId());
            return saved;
        }

        resolveWinnerFromScores(result);
        if (result.getFinishedAt() == null) {
            result.setFinishedAt(LocalDateTime.now());
        }
        BattleResult saved = battleResultRepository.save(result);
        activeBattleService.unregisterBattle(saved.getMatchId());
        return saved;
    }

    private void mergeStats(BattleResult existing, BattleResult incoming) {
        existing.setPlayer1Score(maxInt(existing.getPlayer1Score(), incoming.getPlayer1Score()));
        existing.setPlayer2Score(maxInt(existing.getPlayer2Score(), incoming.getPlayer2Score()));
        existing.setPlayer1AccuracyRate(maxInt(existing.getPlayer1AccuracyRate(), incoming.getPlayer1AccuracyRate()));
        existing.setPlayer2AccuracyRate(maxInt(existing.getPlayer2AccuracyRate(), incoming.getPlayer2AccuracyRate()));
        existing.setPlayer1TypedChars(maxInt(existing.getPlayer1TypedChars(), incoming.getPlayer1TypedChars()));
        existing.setPlayer1MissCount(maxInt(existing.getPlayer1MissCount(), incoming.getPlayer1MissCount()));
        existing.setPlayer2TypedChars(maxInt(existing.getPlayer2TypedChars(), incoming.getPlayer2TypedChars()));
        existing.setPlayer2MissCount(maxInt(existing.getPlayer2MissCount(), incoming.getPlayer2MissCount()));
    }

    /**
     * スコア差で勝者を決定する。同点の場合は winnerId を変更しない（KO 等のクライアント判定を保持）
     */
    private void resolveWinnerFromScores(BattleResult result) {
        Integer player1Score = result.getPlayer1Score();
        Integer player2Score = result.getPlayer2Score();
        if (player1Score == null || player2Score == null) {
            return;
        }
        if (player1Score > player2Score) {
            result.setWinnerId(result.getPlayer1Id());
        } else if (player2Score > player1Score) {
            result.setWinnerId(result.getPlayer2Id());
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
        BattleResult result = battleResultRepository.findById(matchId).orElse(null);
        if (result != null) {
            resolveWinnerFromScores(result);
        }
        return result;
    }
}
