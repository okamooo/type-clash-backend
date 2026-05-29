package com.example.typing.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketSessionRegistry {

    private final Map<String, Long> sessionToUserId = new ConcurrentHashMap<>();

    public void bind(String sessionId, Long userId) {
        if (sessionId == null || userId == null) {
            return;
        }
        sessionToUserId.put(sessionId, userId);
    }

    public Long unbind(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        return sessionToUserId.remove(sessionId);
    }
}
