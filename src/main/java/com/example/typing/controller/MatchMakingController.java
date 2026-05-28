package com.example.typing.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.typing.dto.request.BattleQueueRequest;
import com.example.typing.service.MatchMakingService;

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
    public void joinQueue(@Payload(required = false) BattleQueueRequest request, Principal principal) {
        Long userId = getAuthenticatedUserId(principal);
        if (userId != null) {
            matchMakingService.joinQueue(userId);
        }
    }

    /**
     * マッチング待機列から離脱する (WebSocket用)
     * 送信先: /api/battles/queue/leave
     */
    @MessageMapping("/battles/queue/leave")
    public void leaveQueue(@Payload(required = false) BattleQueueRequest request, Principal principal) {
        Long userId = getAuthenticatedUserId(principal);
        if (userId != null) {
            matchMakingService.leaveQueue(userId);
        }
    }

    /**
     * 成立済み対戦から離脱する (WebSocket用)
     * 送信先: /api/battles/match/leave
     */
    @MessageMapping("/battles/match/leave")
    public void leaveMatch(@Payload(required = false) BattleQueueRequest request, Principal principal) {
        Long userId = getAuthenticatedUserId(principal);
        if (userId != null) {
            matchMakingService.leaveMatch(userId);
        }
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
