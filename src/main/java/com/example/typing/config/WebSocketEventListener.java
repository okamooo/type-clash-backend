package com.example.typing.config;

import com.example.typing.service.ActiveBattleService;
import com.example.typing.service.WebSocketSessionRegistry;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private final WebSocketSessionRegistry sessionRegistry;
    private final ActiveBattleService activeBattleService;

    public WebSocketEventListener(
            WebSocketSessionRegistry sessionRegistry,
            ActiveBattleService activeBattleService) {
        this.sessionRegistry = sessionRegistry;
        this.activeBattleService = activeBattleService;
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        if (sessionId == null) {
            return;
        }

        Long userId = sessionRegistry.unbind(sessionId);
        if (userId != null) {
            activeBattleService.handlePlayerDisconnect(userId);
        }
    }
}
