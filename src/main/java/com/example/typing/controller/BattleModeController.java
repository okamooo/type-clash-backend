package com.example.typing.controller;

import com.example.typing.dto.response.BattlePlayerResultResponse;
import com.example.typing.dto.response.BattleResultResponse;
import com.example.typing.entity.BattleResult;
import com.example.typing.entity.MagicWords;
import com.example.typing.entity.User;
import com.example.typing.repository.UserRepository;
import com.example.typing.security.WebSocketAuthHelper;
import com.example.typing.service.BattleModeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BattleModeController {

    private final BattleModeService battleModeService;
    private final UserRepository userRepository;

    public BattleModeController(BattleModeService battleModeService, UserRepository userRepository) {
        this.battleModeService = battleModeService;
        this.userRepository = userRepository;
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
    public ResponseEntity<?> submitBattleResult(@RequestBody BattleResult result, Principal principal) {
        Long userId = WebSocketAuthHelper.getUserIdFromPrincipal(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "認証が必要です"));
        }
        if (result.getMatchId() == null || result.getMatchId() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "対戦IDが不足しています"));
        }
        if (!battleModeService.isParticipant(result.getMatchId(), userId)) {
            BattleResult existing = battleModeService.getBattleResult(result.getMatchId());
            if (existing == null) {
                return ResponseEntity.status(404).body(Map.of("message", "対戦結果が見つかりません"));
            }
            return ResponseEntity.status(403).body(Map.of("message", "この対戦の参加者ではありません"));
        }

        try {
            BattleResult saved = battleModeService.saveBattleResult(result, userId);
            if (saved == null) {
                return ResponseEntity.status(404).body(Map.of("message", "対戦結果が見つかりません"));
            }
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(Map.of("message", "この対戦の参加者ではありません"));
        }
    }

    /**
     * 【対戦結果取得】
     * 指定した対戦ID（matchId）に紐づく対戦結果を取得する
     */
    @GetMapping("/battle-results")
    public ResponseEntity<?> getBattleResult(@RequestParam(name = "id") Long id, Principal principal) {
        Long userId = WebSocketAuthHelper.getUserIdFromPrincipal(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "認証が必要です"));
        }
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "不正な対戦IDです"));
        }
        if (!battleModeService.isParticipant(id, userId)) {
            BattleResult existing = battleModeService.getBattleResult(id);
            if (existing == null) {
                return ResponseEntity.status(404).body(Map.of("message", "対戦結果が見つかりません"));
            }
            return ResponseEntity.status(403).body(Map.of("message", "この対戦の参加者ではありません"));
        }

        BattleResult result = battleModeService.getBattleResult(id);
        if (result == null) {
            return ResponseEntity.status(404).body(Map.of("message", "対戦結果が見つかりません"));
        }

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

        BattleResultResponse response = BattleResultResponse.builder()
                .id(result.getMatchId())
                .winnerId(result.getWinnerId())
                .finishedAt(result.getFinishedAt() != null ? result.getFinishedAt().toString() : "")
                .players(players)
                .build();

        return ResponseEntity.ok(response);
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
}
