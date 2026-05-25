package com.example.typing.controller;

import com.example.typing.dto.BattleMessage;
import com.example.typing.service.WebSocketSessionRegistry;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class BattleMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionRegistry sessionRegistry;

    public BattleMessageController(
            SimpMessagingTemplate messagingTemplate,
            WebSocketSessionRegistry sessionRegistry) {
        this.messagingTemplate = messagingTemplate;
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * 対戦中のステータス（スコア・進捗など）をリアルタイムに同期する
     * 送信先: /api/battles/{matchId}/update
     * 配信先: /topic/battle/{matchId}
     */
    @MessageMapping("/battles/{matchId}/update")
    public void updateBattleStatus(
            @DestinationVariable Long matchId,
            BattleMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        if (headerAccessor != null && headerAccessor.getSessionId() != null && message.getUserId() != null) {
            sessionRegistry.bind(headerAccessor.getSessionId(), message.getUserId());
        }

        messagingTemplate.convertAndSend("/topic/battle/" + matchId, message);
    }
}
