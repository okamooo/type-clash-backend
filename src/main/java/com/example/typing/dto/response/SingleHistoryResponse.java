package com.example.typing.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SingleHistoryResponse {

    private Integer bestScore;    // 最高スコア
    private Double averageScore;  // 平均スコア
    private Integer playCount;    // プレイ回数
    private List<Entry> histories; // 履歴一覧（finished_at 降順）

    @Data
    @AllArgsConstructor
    public static class Entry {
        private LocalDateTime finishedAt;   // プレイ日時
        private Integer score;       // スコア
        private Integer accuracyRate; // 正答率（%）
    }
}
