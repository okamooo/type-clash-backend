package com.example.typing.controller;

import com.example.typing.dto.BattleMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class BattleMessageController {

    private final SimpMessagingTemplate messagingTemplate;

    public BattleMessageController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 対戦中のステータス（スコア・進捗など）をリアルタイムに同期する
     * 送信先: /api/battles/{matchId}/update
     * 配信先: /topic/battle/{matchId}
     */
    @MessageMapping("/battles/{matchId}/update")
    public void updateBattleStatus(
            @DestinationVariable Long matchId,
            BattleMessage message) {

        // 届いたメッセージを、同じmatchIdのトピックを購読している全員に転送する
        messagingTemplate.convertAndSend("/topic/battle/" + matchId, message);
    }
}
