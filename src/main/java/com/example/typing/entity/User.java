package com.example.typing.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

// ユーザーを表すエンティティクラス
@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID

    @Column(nullable = false)
    private String name; // ユーザー名

    @Column(nullable = false, unique = true)
    private String email; // メールアドレス

    @Column(nullable = false)
    private String password; // パスワード

    @Column(name = "icon_image")
    private String iconImage; // アイコン画像URL

    @Column(name = "background_image", nullable = false)
    private Integer backgroundImage; // 背景画像ID（0 or 1）

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 作成日時

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 更新日時

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // 削除日時（論理削除用）

    // レコード作成時に作成日時・更新日時を自動設定
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // レコード更新時に更新日時を自動更新
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
