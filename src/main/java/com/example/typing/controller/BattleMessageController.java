package com.example.typing.controller;

import com.example.typing.dto.BattleMessage;
import com.example.typing.security.WebSocketAuthHelper;
import com.example.typing.service.ActiveBattleService;
import com.example.typing.service.BattleModeService;
import com.example.typing.service.WebSocketSessionRegistry;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class BattleMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionRegistry sessionRegistry;
    private final BattleModeService battleModeService;
    private final ActiveBattleService activeBattleService;

    public BattleMessageController(
            SimpMessagingTemplate messagingTemplate,
            WebSocketSessionRegistry sessionRegistry,
            BattleModeService battleModeService,
            ActiveBattleService activeBattleService) {
        this.messagingTemplate = messagingTemplate;
        this.sessionRegistry = sessionRegistry;
        this.battleModeService = battleModeService;
        this.activeBattleService = activeBattleService;
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
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {

        Long userId = WebSocketAuthHelper.getUserIdFromPrincipal(principal);
        if (userId == null) {
            throw new MessageDeliveryException("Unauthorized: not authenticated");
        }
        if (!battleModeService.isParticipant(matchId, userId)) {
            throw new MessageDeliveryException("Unauthorized: not a participant of this match");
        }

        if (headerAccessor != null && headerAccessor.getSessionId() != null) {
            sessionRegistry.bind(headerAccessor.getSessionId(), userId);
        }

        message.setUserId(userId);
        message.setMatchId(matchId);
        battleModeService.recordBattleUpdate(matchId, userId, message);
        activeBattleService.syncBattleUpdate(matchId, userId, message);
        messagingTemplate.convertAndSend("/topic/battle/" + matchId, message);
    }
}
