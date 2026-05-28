package com.example.typing.security;

import java.security.Principal;
import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String MATCH_NOTIFICATION_TOPIC_PREFIX = "/topic/match/notification/";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            handleConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            handleSubscribe(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        Long userId = getHandshakeUserId(accessor);
        if (userId == null) {
            throw new MessageDeliveryException("Unauthorized: not authenticated");
        }
        accessor.setUser(WebSocketAuthHelper.createPrincipal(userId));
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(MATCH_NOTIFICATION_TOPIC_PREFIX)) {
            return;
        }

        Principal user = accessor.getUser();
        if (user == null) {
            throw new MessageDeliveryException("Unauthorized: not authenticated");
        }

        Long userId = WebSocketAuthHelper.getUserIdFromPrincipal(user);
        if (userId == null) {
            throw new MessageDeliveryException("Unauthorized: invalid principal");
        }

        String topicUserId = destination.substring(destination.lastIndexOf('/') + 1);
        if (!topicUserId.equals(String.valueOf(userId))) {
            throw new MessageDeliveryException("Unauthorized subscription");
        }
    }

    private Long getHandshakeUserId(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return null;
        }
        Object value = sessionAttributes.get(WebSocketAuthHelper.WS_USER_ID_ATTR);
        if (value instanceof Long userId) {
            return userId;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
