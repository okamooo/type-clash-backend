package com.example.typing.security;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.example.typing.service.LoginSessionService;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;
    private final LoginSessionService loginSessionService;

    public JwtHandshakeInterceptor(JwtUtils jwtUtils, LoginSessionService loginSessionService) {
        this.jwtUtils = jwtUtils;
        this.loginSessionService = loginSessionService;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        String token = WebSocketAuthHelper.parseAccessToken(
                servletRequest.getServletRequest().getCookies());
        if (token == null || !jwtUtils.validateToken(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        String loginSessionId = jwtUtils.getLoginSessionIdFromToken(token);
        if (!loginSessionService.isValid(userId, loginSessionId)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put(WebSocketAuthHelper.WS_USER_ID_ATTR, userId);
        attributes.put(WebSocketAuthHelper.WS_LOGIN_SESSION_ID_ATTR, loginSessionId);
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // no-op
    }
}
