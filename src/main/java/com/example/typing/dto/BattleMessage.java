package com.example.typing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattleMessage {
    private Long userId;        // 誰のデータか
    private Long matchId;       // どの対戦ルームか
    private Integer score;      // 現在のスコア
    private Integer accuracyRate; // 現在の正答率
    private Integer typedChars;  // 入力文字数
    private Integer missCount;   // ミス数
    private Integer currentHp;   // 現在のHP
    private Integer damage;      // 与えたダメージ
    private String messageId;    // メッセージの重複判定用ID
    private String content;     // 補足メッセージ（例：「完了！」など）
}
