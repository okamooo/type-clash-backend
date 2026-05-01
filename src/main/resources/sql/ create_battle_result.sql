CREATE TABLE battle_results (
    id SERIAL PRIMARY KEY,                   -- 対戦ID（自動採番）
    player1_id INTEGER NOT NULL,            -- プレイヤーAのID
    player2_id INTEGER NOT NULL,            -- プレイヤーBのID
    winner_id INTEGER,                      -- 勝者のID（引き分けの場合はNULL）
    player1_score INTEGER NOT NULL DEFAULT 0, -- スコア1
    player2_score INTEGER NOT NULL DEFAULT 0, -- スコア2
    player1_accuracy_rate INTEGER NOT NULL, -- 正答率1
    player2_accuracy_rate INTEGER NOT NULL, -- 正答率2
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 開始日時

    -- 外部キー制約（usersテーブルがある場合）
    CONSTRAINT fk_player1 FOREIGN KEY (player1_id) REFERENCES users(id),
    CONSTRAINT fk_player2 FOREIGN KEY (player2_id) REFERENCES users(id),
    CONSTRAINT fk_winner FOREIGN KEY (winner_id) REFERENCES users(id)
);