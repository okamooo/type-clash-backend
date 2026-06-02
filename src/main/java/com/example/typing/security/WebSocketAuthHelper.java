package com.example.typing.security;

import java.security.Principal;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.Cookie;

public final class WebSocketAuthHelper {

    public static final String WS_USER_ID_ATTR = "wsUserId";
    public static final String WS_LOGIN_SESSION_ID_ATTR = "wsLoginSessionId";

    private WebSocketAuthHelper() {
    }

    public static String parseAccessToken(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static Principal createPrincipal(Long userId) {
        return new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
    }

    public static Long getUserIdFromPrincipal(Principal principal) {
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
