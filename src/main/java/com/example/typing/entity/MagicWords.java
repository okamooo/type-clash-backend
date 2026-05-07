package com.example.typing.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "magic_words")
public class MagicWords {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "magic_id")
    private Long magicId;

    @Column(name = "magic_text")
    private String magicText;

    @Column(name = "magic_reading")
    private String magicReading;

    @Column(name = "magic_target")
    private String magicTarget;
}
