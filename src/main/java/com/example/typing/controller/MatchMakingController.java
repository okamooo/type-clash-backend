package com.example.typing.controller;

import com.example.typing.service.MatchMakingService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class MatchMakingController {

    private final MatchMakingService matchMakingService;

    public MatchMakingController(MatchMakingService matchMakingService) {
        this.matchMakingService = matchMakingService;
    }

    /**
     * マッチング待機列に参加する
     * 送信先: /api/battles/queue/join
     */
    @MessageMapping("/battles/queue/join")
    public void joinQueue(@Payload Map<String, Object> payload) {
        if (payload.containsKey("userId")) {
            Long userId = Long.valueOf(payload.get("userId").toString());
            matchMakingService.joinQueue(userId);
        }
    }

    /**
     * マッチング待機列から離脱する
     * 送信先: /api/battles/queue/leave
     */
    @MessageMapping("/battles/queue/leave")
    public void leaveQueue(@Payload Map<String, Object> payload) {
        if (payload.containsKey("userId")) {
            Long userId = Long.valueOf(payload.get("userId").toString());
            matchMakingService.leaveQueue(userId);
        }
    }
}
