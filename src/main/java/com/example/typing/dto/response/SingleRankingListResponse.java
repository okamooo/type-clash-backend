package com.example.typing.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SingleRankingListResponse {

    private Integer totalUsers;                  // 総ユーザー数
    private List<SingleRankingResponse> rankings; // ランキング一覧
}
