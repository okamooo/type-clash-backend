package com.example.typing.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.typing.dto.request.BattleQueueRequest;
import com.example.typing.service.ActiveBattleService;
import com.example.typing.service.MatchMakingService;
import com.example.typing.service.WebSocketSessionRegistry;

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
    public void joinQueue(
            @Payload(required = false) BattleQueueRequest request,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {
        Long userId = getAuthenticatedUserId(principal);
        if (userId != null) {
            bindSession(headerAccessor, userId);
            matchMakingService.joinQueue(userId);
        }
    }

    /**
     * マッチング待機列から離脱する (WebSocket用)
     * 送信先: /api/battles/queue/leave
     */
    @MessageMapping("/battles/queue/leave")
    public void leaveQueue(
            @Payload(required = false) BattleQueueRequest request,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {
        Long userId = getAuthenticatedUserId(principal);
        if (userId != null) {
            bindSession(headerAccessor, userId);
            matchMakingService.leaveQueue(userId);
        }
    }

    /**
     * 対戦準備完了を報告する
     * 送信先: /api/battles/ready
     */
    @MessageMapping("/battles/ready")
    public void handleReady(
            @Payload(required = false) BattleQueueRequest request,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {
        Long userId = getAuthenticatedUserId(principal);
        if (userId != null) {
            bindSession(headerAccessor, userId);
            matchMakingService.playerReady(userId);
        }
    }

    /**
     * 成立済み対戦から離脱する (WebSocket用)
     * 送信先: /api/battles/match/leave
     */
    @MessageMapping("/battles/match/leave")
    public void leaveMatch(
            @Payload(required = false) BattleQueueRequest request,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {
        Long userId = getAuthenticatedUserId(principal);
        if (userId != null) {
            bindSession(headerAccessor, userId);
            matchMakingService.leaveMatch(userId);
        }
    }

    /**
     * 対戦中の離脱（敗北）を報告する
     * 送信先: /api/battles/forfeit
     */
    @MessageMapping("/battles/forfeit")
    public void forfeit(
            @Payload(required = false) BattleQueueRequest request,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {
        Long userId = getAuthenticatedUserId(principal);
        if (userId == null) {
            return;
        }
        bindSession(headerAccessor, userId);
        activeBattleService.forfeit(userId, null);
    }

    /**
     * マッチング待機列から離脱する (REST用: ブラウザ離脱時の sendBeacon 等)
     */
    @PostMapping("/api/battles/queue/leave")
    public void leaveQueueRest(Principal principal) {
        Long userId = getAuthenticatedUserId(principal);
        if (userId != null) {
            matchMakingService.leaveQueue(userId);
        }
    }

    /**
     * 成立済み対戦から離脱する (REST用: ブラウザ離脱時の sendBeacon 等)
     */
    @PostMapping("/api/battles/match/leave")
    public void leaveMatchRest(Principal principal) {
        Long userId = getAuthenticatedUserId(principal);
        if (userId != null) {
            matchMakingService.leaveMatch(userId);
        }
    }

    /**
     * 対戦中の離脱 (REST用: ブラウザ終了時の sendBeacon 等)
     */
    @PostMapping("/api/battles/forfeit")
    public void forfeitRest(Principal principal) {
        Long userId = getAuthenticatedUserId(principal);
        if (userId != null) {
            activeBattleService.forfeit(userId, null);
        }
    }

    private void bindSession(SimpMessageHeaderAccessor headerAccessor, Long userId) {
        if (headerAccessor != null && headerAccessor.getSessionId() != null) {
            sessionRegistry.bind(headerAccessor.getSessionId(), userId);
        }
    }

    private Long getAuthenticatedUserId(Principal principal) {
        if (principal == null) {
            return null;
        }
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof Long userId) {
            return userId;
        }
        if (principal instanceof UsernamePasswordAuthenticationToken token
                && token.getPrincipal() instanceof Long userId) {
            return userId;
        }
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
