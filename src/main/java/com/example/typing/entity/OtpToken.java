package com.example.typing.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * OTP 認証用の一時ユーザー情報エンティティ。
 * ユーザー登録リクエスト受信後、OTP 検証が完了するまでの間、
 * 仮登録データとして otp_tokens テーブルに保持される。
 * 検証成功後は users テーブルへ本登録され、このレコードは削除される。
 */
@Entity
@Data
@Table(name = "otp_tokens")
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //id

    @Column(nullable = false)
    private String name; //名前

    @Column(nullable = false, unique = true)
    private String email; //メールアドレス

    @Column(nullable = false)
    private String password; // パスワード

    @Column(name = "otp_hash", nullable = false)
    private String otpHash; //OTP(ハッシュ化)

    @Column(nullable = false)
    private LocalDateTime expiry; //有効期限

    @Column(name = "fail_count", nullable = false)
    private int failCount = 0; //OTP失敗回数

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 作成日時

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
