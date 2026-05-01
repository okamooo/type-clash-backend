CREATE TABLE users (
    id SERIAL PRIMARY KEY,                    -- ユーザーID（自動採番）
    name VARCHAR(50) NOT NULL,               -- ユーザー名
    email VARCHAR(255) NOT NULL UNIQUE,      -- メールアドレス（重複不可）
    password VARCHAR(255) NOT NULL,          -- ハッシュ化済みパスワード
    icon_image VARCHAR(255),                 -- アイコン画像のパスまたはURL
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 作成日時
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 更新日時
    deleted_at TIMESTAMP                     -- 論理削除日時（未削除時はNULL）
);
