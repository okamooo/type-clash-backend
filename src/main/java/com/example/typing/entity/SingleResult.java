package com.example.typing.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "single_results")
public class SingleResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId; // FK: users.id

    private Integer score;

    @Column(name = "accuracy_rate")
    private Integer accuracyRate; // 正答率

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
}