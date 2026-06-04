package com.example.typing.controller;

import com.example.typing.entity.BattleResult;
import com.example.typing.entity.MagicWords;
import com.example.typing.security.WebSocketAuthHelper;
import com.example.typing.service.BattleModeService;
import com.example.typing.service.BattleResultAccess;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
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
    public ResponseEntity<?> submitBattleResult(@RequestBody BattleResult result, Principal principal) {
        Long userId = WebSocketAuthHelper.getUserIdFromPrincipal(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "認証が必要です"));
        }
        if (result.getMatchId() == null || result.getMatchId() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "対戦IDが不足しています"));
        }

        BattleResultAccess access = battleModeService.resolveBattleResultAccess(result.getMatchId(), userId);
        if (access.status() == BattleResultAccess.Status.NOT_FOUND) {
            return ResponseEntity.status(404).body(Map.of("message", "対戦結果が見つかりません"));
        }
        if (access.status() == BattleResultAccess.Status.FORBIDDEN) {
            return ResponseEntity.status(403).body(Map.of("message", "この対戦の参加者ではありません"));
        }

        try {
            BattleResult saved = battleModeService.saveBattleResult(result, userId, access.record());
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

        BattleResultAccess access = battleModeService.resolveBattleResultAccess(id, userId);
        if (access.status() == BattleResultAccess.Status.NOT_FOUND) {
            return ResponseEntity.status(404).body(Map.of("message", "対戦結果が見つかりません"));
        }
        if (access.status() == BattleResultAccess.Status.FORBIDDEN) {
            return ResponseEntity.status(403).body(Map.of("message", "この対戦の参加者ではありません"));
        }

        return ResponseEntity.ok(access.response());
    }
}
