CREATE TABLE magic_words (
    magic_id SERIAL PRIMARY KEY,            -- クイズID（自動採番）
    magic_text VARCHAR(255) NOT NULL,      -- ローマ字テキスト（画面表示用）
    magic_reading VARCHAR(255) NOT NULL,   -- カナ文字呪文表記
    magic_target VARCHAR(255) NOT NULL     -- タイピング判定の正解
);