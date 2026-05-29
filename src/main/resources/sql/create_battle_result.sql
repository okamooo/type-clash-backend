DROP TABLE IF EXISTS battle_results;

CREATE TABLE battle_results (
    match_id BIGINT PRIMARY KEY,
    player1_id BIGINT NOT NULL,
    player2_id BIGINT NOT NULL,
    winner_id BIGINT,
    player1_score INT,
    player2_score INT,
    player1_accuracy_rate INT,
    player2_accuracy_rate INT,
    player1_typed_chars INT,
    player1_miss_count INT,
    player2_typed_chars INT,
    player2_miss_count INT,
    player1_hp INT,
    player2_hp INT,
    finished_at TIMESTAMP
);
