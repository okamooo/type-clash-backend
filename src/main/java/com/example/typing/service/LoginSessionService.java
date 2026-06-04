package com.example.typing.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoginSessionService {

    private static final Duration SESSION_TTL = Duration.ofHours(24);
    private static final String LOGIN_SESSION_KEY_PREFIX = "login:user:";

    private final StringRedisTemplate redisTemplate;

    public LoginSessionService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String createSession(Long userId) {
        String loginSessionId = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(buildKey(userId), loginSessionId, SESSION_TTL);
        return loginSessionId;
    }

    public boolean isValid(Long userId, String loginSessionId) {
        if (userId == null || loginSessionId == null || loginSessionId.isBlank()) {
            return false;
        }
        String currentSessionId = redisTemplate.opsForValue().get(buildKey(userId));
        return loginSessionId.equals(currentSessionId);
    }

    public void deleteSession(Long userId) {
        if (userId == null) {
            return;
        }
        redisTemplate.delete(buildKey(userId));
    }

    public void deleteSessionIfMatches(Long userId, String loginSessionId) {
        if (!isValid(userId, loginSessionId)) {
            return;
        }
        deleteSession(userId);
    }

    private String buildKey(Long userId) {
        return LOGIN_SESSION_KEY_PREFIX + userId;
    }
}
