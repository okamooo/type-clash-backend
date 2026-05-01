# ER図

```mermaid
erDiagram
    users {
        SERIAL id PK
        VARCHAR name
        VARCHAR email
        VARCHAR password
        VARCHAR icon_image
        TIMESTAMP created_at
        TIMESTAMP updated_at
        TIMESTAMP deleted_at
        INTEGER background_image
    }

    words {
        BIGSERIAL id PK
        VARCHAR display_text
        VARCHAR kana_reading
        VARCHAR romaji_target
    }

    magic_words {
        SERIAL magic_id PK
        VARCHAR magic_text
        VARCHAR magic_reading
        VARCHAR magic_target
    }

    single_results {
        SERIAL id PK
        INTEGER user_id FK
        INTEGER score
        INTEGER accuracy_rate
        TIMESTAMP started_at
    }

    battle_results {
        SERIAL id PK
        INTEGER player1_id FK
        INTEGER player2_id FK
        INTEGER winner_id FK
        INTEGER player1_score
        INTEGER player2_score
        INTEGER player1_accuracy_rate
        INTEGER player2_accuracy_rate
        TIMESTAMP started_at
    }

    users ||--o{ single_results : has_single_results
    users ||--o{ battle_results : as_player1
    users ||--o{ battle_results : as_player2
    users |o--o{ battle_results : as_winner
