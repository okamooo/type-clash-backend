package com.example.typing.controller;

import com.example.typing.service.MatchMakingService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
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
     * マッチング待機列から離脱するメソッド (WebSocket用)
     * 送信先: /api/battles/queue/leave
     */
    @MessageMapping("/battles/queue/leave")
    public void leaveQueue(@Payload Map<String, Object> payload) {
        if (payload.containsKey("userId")) {
            Long userId = Long.valueOf(payload.get("userId").toString());
            matchMakingService.leaveQueue(userId);
        }
    }

    /**
     * マッチング待機列から離脱するメソッド (REST用: ブラウザ終了時の sendBeacon 等)
     * 送信先: /api/battles/queue/leave
     */
    @PostMapping("/api/battles/queue/leave")
    public void leaveQueueRest(@RequestBody Map<String, Object> payload) {
        if (payload.containsKey("userId")) {
            Long userId = Long.valueOf(payload.get("userId").toString());
            matchMakingService.leaveQueue(userId);
        }
    }
}
