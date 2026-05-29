package com.example.typing.dto;

import com.example.typing.dto.response.BattlePlayerResultResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BattlePlayerResultResponseTest {

    @Test
    void serializesIsWinnerFieldName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BattlePlayerResultResponse player = BattlePlayerResultResponse.builder()
                .id(1L)
                .role("player1")
                .score(100)
                .isWinner(true)
                .build();

        JsonNode json = mapper.readTree(mapper.writeValueAsString(player));
        assertTrue(json.has("isWinner"), "JSON must contain isWinner, got: " + json);
        assertTrue(json.get("isWinner").asBoolean());
    }
}
