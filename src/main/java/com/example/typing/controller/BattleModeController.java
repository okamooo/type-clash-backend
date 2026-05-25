package com.example.typing.controller;

import com.example.typing.dto.response.BattlePlayerResultResponse;
import com.example.typing.dto.response.BattleResultResponse;
import com.example.typing.entity.BattleResult;
import com.example.typing.entity.MagicWords;
import com.example.typing.service.BattleModeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class BattleModeController {

    private final BattleModeService battleModeService;

    public BattleModeController(BattleModeService battleModeService) {
        this.battleModeService = battleModeService;
    }

    /**
     * 【マジックワード取得】
     * 対戦モードで使用する単語を全件取得する
     */
    @GetMapping("/magic-words")
    public ResponseEntity<?> getMagicWords() {
        List<MagicWords> words = battleModeService.getAllMagicWords();
        if (words.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "マジックワードが見つかりません"));
        }
        return ResponseEntity.ok(words);
    }

    /**
     * 【対戦結果登録】
     * 対戦の結果を登録する
     */
    @PostMapping("/battle-results")
    public ResponseEntity<?> submitBattleResult(@RequestBody BattleResult result) {
        if (result.getMatchId() == null || result.getMatchId() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "対戦IDが不足しています"));
        }
        if (result.getPlayer1Id() == null || result.getPlayer2Id() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "プレイヤーIDが不足しています"));
        }
        BattleResult saved = battleModeService.saveBattleResult(result);
        return ResponseEntity.ok(saved);
    }

    /**
     * 【対戦結果取得】
     * 指定した対戦ID（matchId）に紐づく対戦結果を取得する
     */
    @GetMapping("/battle-results")
    public ResponseEntity<?> getBattleResult(@RequestParam(name = "id") Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "不正な対戦IDです"));
        }
        BattleResult result = battleModeService.getBattleResult(id);
        if (result == null) {
            return ResponseEntity.status(404).body(Map.of("message", "対戦結果が見つかりません"));
        }

        // フロントエンドの「絶対の型」に合わせて詰め替える（id = matchId）
        List<BattlePlayerResultResponse> players = new ArrayList<>();
        
        // Player 1
        players.add(BattlePlayerResultResponse.builder()
                .id(result.getPlayer1Id())
                .role("player1")
                .score(result.getPlayer1Score())
                .accuracyRate(result.getPlayer1AccuracyRate())
                .typedChars(result.getPlayer1TypedChars())
                .missCount(result.getPlayer1MissCount())
                .isWinner(result.getWinnerId() != null && result.getWinnerId().equals(result.getPlayer1Id()))
                .build());

        // Player 2
        players.add(BattlePlayerResultResponse.builder()
                .id(result.getPlayer2Id())
                .role("player2")
                .score(result.getPlayer2Score())
                .accuracyRate(result.getPlayer2AccuracyRate())
                .typedChars(result.getPlayer2TypedChars())
                .missCount(result.getPlayer2MissCount())
                .isWinner(result.getWinnerId() != null && result.getWinnerId().equals(result.getPlayer2Id()))
                .build());

        BattleResultResponse response = BattleResultResponse.builder()
                .id(result.getMatchId())
                .winnerId(result.getWinnerId())
                .finishedAt(result.getFinishedAt() != null ? result.getFinishedAt().toString() : "")
                .players(players)
                .build();

        return ResponseEntity.ok(response);
    }
}
