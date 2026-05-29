package com.example.typing.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "battle_results")
public class BattleResult {
    @Id
    @Column(name = "match_id")
    private Long matchId;

    @Column(name = "player1_id")
    private Long player1Id;

    @Column(name = "player2_id")
    private Long player2Id;

    @Column(name = "winner_id")
    private Long winnerId;

    @Column(name = "player1_score")
    private Integer player1Score;

    @Column(name = "player2_score")
    private Integer player2Score;

    @Column(name = "player1_accuracy_rate")
    private Integer player1AccuracyRate;

    @Column(name = "player2_accuracy_rate")
    private Integer player2AccuracyRate;

    @Column(name = "player1_typed_chars")
    private Integer player1TypedChars;

    @Column(name = "player1_miss_count")
    private Integer player1MissCount;

    @Column(name = "player2_typed_chars")
    private Integer player2TypedChars;

    @Column(name = "player2_miss_count")
    private Integer player2MissCount;

    @Column(name = "player1_hp")
    private Integer player1Hp;

    @Column(name = "player2_hp")
    private Integer player2Hp;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
}
