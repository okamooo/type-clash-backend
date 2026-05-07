package com.example.typing.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "battle_results")
public class BattleResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
}
