package com.example.typing.controller;

import com.example.typing.dto.ScoreRequest;
import com.example.typing.entity.SingleResult;
import com.example.typing.entity.Words;
import com.example.typing.service.SingleModeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
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
    public List<Words> getWords() {
        return wordService.getAllWords();
    }

    /**
     * 【スコア保存】
     * シングルモードのプレイ結果（ユーザーID・スコア・正答率など）を登録する
     */
    @PostMapping("/single-results")
    public int submitScore(@RequestBody ScoreRequest request) {
        return wordService.calculateAndSaveScore(
                request.getUserId(),
                request.getRawScore(),
                request.getCorrectCount(),
                request.getTotalInput());
    }

    /**
     * 【最新結果取得】
     * 指定したユーザーの最近のシングルモード結果を1件取得する
     */
    @GetMapping("/single-results/latest")
    public SingleResult getLatestResult(@RequestParam Integer userId) {
        return wordService.getLatestResult(userId);
    }

    /**
     * 【ランキング取得】
     * シングルモードのスコア順ランキングを取得する
     */
    @GetMapping("/single-results/rankings")
    public List<SingleResult> getRankings() {
        return wordService.getRankings();
    }
}