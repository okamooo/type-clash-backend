package com.example.typing.security;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;

    public StompAuthChannelInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String token = extractToken(accessor);
        if (token != null && jwtUtils.validateToken(token)) {
            Long userId = jwtUtils.getUserIdFromToken(token);
            Principal principal = new UsernamePasswordAuthenticationToken(
                    userId, null, Collections.emptyList());
            accessor.setUser(principal);
        }

        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        List<String> authorizationHeaders = accessor.getNativeHeader("Authorization");
        if (authorizationHeaders != null && !authorizationHeaders.isEmpty()) {
            String bearer = authorizationHeaders.get(0);
            if (bearer.startsWith("Bearer ")) {
                return bearer.substring(7);
            }
        }

        List<String> cookieHeaders = accessor.getNativeHeader("Cookie");
        if (cookieHeaders != null && !cookieHeaders.isEmpty()) {
            return parseAccessTokenFromCookie(cookieHeaders.get(0));
        }

        return null;
    }

    private String parseAccessTokenFromCookie(String cookieHeader) {
        for (String part : cookieHeader.split(";")) {
            String trimmed = part.trim();
            if (trimmed.startsWith("accessToken=")) {
                return trimmed.substring("accessToken=".length());
            }
        }
        return null;
    }
}
