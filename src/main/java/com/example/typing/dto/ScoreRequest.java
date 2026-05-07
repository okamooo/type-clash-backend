package com.example.typing.dto;

import lombok.Data;

@Data
public class ScoreRequest {
    private Integer userId; // ユーザーID
    private int rawScore; // 暫定スコア
    private int correctCount; // 正解入力数
    private int totalInput; // 総入力数
}