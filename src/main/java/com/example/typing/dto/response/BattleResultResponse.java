package com.example.typing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleResultResponse {
    private Long id;
    private Long winnerId;
    private String finishedAt;
    private List<BattlePlayerResultResponse> players;
}
