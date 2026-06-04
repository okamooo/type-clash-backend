package com.example.typing.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattlePlayerResultResponse {
    private Long id;
    private String name;
    private String iconImage;
    private String role; // "player1" or "player2"
    private Integer score;
    private Integer accuracyRate;
    private Integer typedChars;
    private Integer missCount;
    @JsonProperty("isWinner")
    private boolean isWinner;
}
