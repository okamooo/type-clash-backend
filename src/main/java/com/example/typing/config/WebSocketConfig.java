package com.example.typing.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // /topic で始まるメッセージをブロードキャスト（全員に送信）対象にする
        // /queue で始まるメッセージを個別送信（convertAndSendToUser）用にする
        config.enableSimpleBroker("/topic", "/queue");
        // ドキュメントのURL（/api/...）に合わせるため、接頭辞を /api に変更
        config.setApplicationDestinationPrefixes("/api");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocketの接続先URLを設定。テスト用に全オリジンを許可。
        // 純粋なWebSocketを使用するように設定（SockJSなし）。
        registry.addEndpoint("/ws-battle")
                .setAllowedOriginPatterns("*");
    }
}
