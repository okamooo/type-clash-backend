package com.example.typing.controller;

import com.example.typing.dto.ScoreRequest;
import com.example.typing.entity.SingleResult;
import com.example.typing.entity.Words;
import com.example.typing.service.SingleModeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = { "http://localhost:3000", "http://127.0.0.1:3000" })
public class SingleModeController {

    private final SingleModeService wordService;

    public SingleModeController(SingleModeService wordService) {
        this.wordService = wordService;
    }

    /**
     * 【単語取得】
     * シングルモードで使用する単語を全件取得する
     */
    @GetMapping("/words")
    public ResponseEntity<?> getWords() {
        List<Words> words = wordService.getAllWords();
        if (words.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "単語データが見つかりません"));
        }
        return ResponseEntity.ok(words);
    }

    /**
     * 【スコア保存】
     * シングルモードのプレイ結果を登録する
     */
    @PostMapping("/single-results")
    public ResponseEntity<?> submitScore(@RequestBody ScoreRequest request) {
        if (request.getUserId() == null || request.getUserId() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "不正なユーザーIDです"));
        }
        if (request.getTotalInput() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "総入力数は1以上である必要があります"));
        }
        int score = wordService.calculateAndSaveScore(
                request.getUserId(),
                request.getRawScore(),
                request.getCorrectCount(),
                request.getTotalInput());
        return ResponseEntity.ok(Map.of("score", score));
    }

    /**
     * 【最新結果取得】
     * 指定したユーザーの最近のシングルモード結果を1件取得する
     */
    @GetMapping("/single-results/latest")
    public ResponseEntity<?> getLatestResult(@RequestParam Long userId) {
        if (userId == null || userId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "不正なユーザーIDです"));
        }
        SingleResult result = wordService.getLatestResult(userId);
        if (result == null) {
            return ResponseEntity.status(404).body(Map.of("message", "結果データが見つかりません"));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 【ランキング取得】
     * シングルモードのスコア順ランキングを取得する
     */
    @GetMapping("/single-results/rankings")
    public ResponseEntity<?> getRankings() {
        List<SingleResult> rankings = wordService.getRankings();
        return ResponseEntity.ok(rankings);
    }
}