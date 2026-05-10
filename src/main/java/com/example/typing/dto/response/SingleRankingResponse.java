package com.example.typing.dto.response;
 
import lombok.AllArgsConstructor;
import lombok.Data;
 
@Data
@AllArgsConstructor
public class SingleRankingResponse {

    private Integer rank;         // 順位
    private Long userId;          // ユーザーID
    private String userName;      // ユーザー名
    private Integer score;        // スコア
    private Integer accuracyRate; // 正答率（%）
}
