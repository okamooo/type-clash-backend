package com.example.typing.controller;

import com.example.typing.service.ActiveBattleService;
import com.example.typing.service.MatchMakingService;
import com.example.typing.service.WebSocketSessionRegistry;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MatchMakingController {

    private final MatchMakingService matchMakingService;
    private final ActiveBattleService activeBattleService;
    private final WebSocketSessionRegistry sessionRegistry;

    public MatchMakingController(
            MatchMakingService matchMakingService,
            ActiveBattleService activeBattleService,
            WebSocketSessionRegistry sessionRegistry) {
        this.matchMakingService = matchMakingService;
        this.activeBattleService = activeBattleService;
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * マッチング待機列に参加する
     * 送信先: /api/battles/queue/join
     */
    @MessageMapping("/battles/queue/join")
    public void joinQueue(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        if (payload.containsKey("userId")) {
            Long userId = Long.valueOf(payload.get("userId").toString());
            bindSession(headerAccessor, userId);
            matchMakingService.joinQueue(userId);
        }
    }

    /**
     * マッチング待機列から離脱するメソッド (WebSocket用)
     * 送信先: /api/battles/queue/leave
     */
    @MessageMapping("/battles/queue/leave")
    public void leaveQueue(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        if (payload.containsKey("userId")) {
            Long userId = Long.valueOf(payload.get("userId").toString());
            bindSession(headerAccessor, userId);
            matchMakingService.leaveQueue(userId);
        }
    }

    /**
     * 対戦準備完了を報告する
     * 送信先: /api/battles/ready
     */
    @MessageMapping("/battles/ready")
    public void handleReady(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        if (payload.containsKey("userId")) {
            Long userId = Long.valueOf(payload.get("userId").toString());
            bindSession(headerAccessor, userId);
            matchMakingService.playerReady(userId);
        }
    }

    /**
     * 対戦中の離脱（敗北）を報告する
     * 送信先: /api/battles/forfeit
     */
    @MessageMapping("/battles/forfeit")
    public void forfeit(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        if (payload.containsKey("userId") && payload.containsKey("matchId")) {
            Long userId = Long.valueOf(payload.get("userId").toString());
            Long matchId = Long.valueOf(payload.get("matchId").toString());
            bindSession(headerAccessor, userId);
            activeBattleService.forfeit(userId, matchId);
        }
    }

    /**
     * マッチング待機列から離脱するメソッド (REST用: ブラウザ終了時の sendBeacon 等)
     */
    @PostMapping("/api/battles/queue/leave")
    public void leaveQueueRest(@RequestBody Map<String, Object> payload) {
        if (payload.containsKey("userId")) {
            Long userId = Long.valueOf(payload.get("userId").toString());
            matchMakingService.leaveQueue(userId);
        }
    }

    /**
     * 対戦中の離脱 (REST用: ブラウザ終了時の sendBeacon 等)
     */
    @PostMapping("/api/battles/forfeit")
    public void forfeitRest(@RequestBody Map<String, Object> payload) {
        if (payload.containsKey("userId") && payload.containsKey("matchId")) {
            Long userId = Long.valueOf(payload.get("userId").toString());
            Long matchId = Long.valueOf(payload.get("matchId").toString());
            activeBattleService.forfeit(userId, matchId);
        }
    }

    private void bindSession(SimpMessageHeaderAccessor headerAccessor, Long userId) {
        if (headerAccessor != null && headerAccessor.getSessionId() != null) {
            sessionRegistry.bind(headerAccessor.getSessionId(), userId);
        }
    }
}
