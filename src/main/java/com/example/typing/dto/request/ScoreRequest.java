package com.example.typing.dto.request;

import lombok.Data;

@Data
public class ScoreRequest {
    private Long userId; // ユーザーID
    private Integer rawScore; // 暫定スコア
    private Integer correctCount; // 正解入力数
    private Integer totalInput; // 総入力数
}
