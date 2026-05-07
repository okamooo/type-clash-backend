CREATE TABLE words (
    id BIGSERIAL PRIMARY KEY,
    display_text VARCHAR(255) NOT NULL,
    kana_reading VARCHAR(255) NOT NULL,
    romaji_target VARCHAR(255) NOT NULL
);