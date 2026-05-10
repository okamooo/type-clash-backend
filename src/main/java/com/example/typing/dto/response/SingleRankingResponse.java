package com.example.typing.dto.response;
 
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
 
@Data
@AllArgsConstructor
public class SingleRankingResponse {

    private Integer totalUsers;      // 総ユーザー数
    private Double averageBestScore; // 平均ベストスコア
    private List<Entry> rankings;    // ランキングリスト

    @Data
    @AllArgsConstructor
    public static class Entry {
        private Integer rank;         // 順位
        private Long userId;          // ユーザーID
        private String userName;      // ユーザー名
        private Integer score;        // スコア
        private Integer accuracyRate; // 正答率（%）
    }
}
