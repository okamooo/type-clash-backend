CREATE TABLE single_results (
    id SERIAL PRIMARY KEY,                   -- ID（自動採番）
    user_id INTEGER NOT NULL,               -- ユーザーID
    score INTEGER NOT NULL DEFAULT 0,       -- スコア
    accuracy_rate INTEGER NOT NULL,         -- 正答率
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 開始日時

    -- 外部キー制約（usersテーブルのidを参照）
    CONSTRAINT fk_single_user FOREIGN KEY (user_id) REFERENCES users(id)
);