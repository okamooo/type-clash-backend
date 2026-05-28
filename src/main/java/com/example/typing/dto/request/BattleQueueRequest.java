package com.example.typing.dto.request;

import lombok.Data;

/**
 * マッチング待機列の WebSocket 操作用 DTO。
 * userId は JWT からサーバー側で取得するため、クライアントからは送らない。
 */
@Data
public class BattleQueueRequest {
}
