package com.example.typing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

// クイズの単語を表すエンティティクラス
@Entity
@Data
@Table(name = "words")
public class Words {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID

    @Column(name = "display_text")
    private String displayText; // ローマ字テキスト（表示用）

    @Column(name = "kana_reading")
    private String kanaReading; // カナ文字表記

    @Column(name = "romaji_target")
    private String romajiTarget; // タイピング判定の正解
}
