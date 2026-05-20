package com.example.typing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.typing.entity.OtpToken;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    /**
     * メールアドレスから OTP トークンを取得する
     *
     * @param email 検索するメールアドレス
     * @return 一致するレコードが存在する場合はその {@link OtpToken}、存在しない場合は空の {@link Optional}
     */
    Optional<OtpToken> findByEmail(String email);

    /**
     * メールアドレスに紐づく OTP トークンを削除する
     *
     * @param email 削除対象のメールアドレス
     */
    void deleteByEmail(String email);

    /**
     * メールアドレスが otp_tokens テーブルに存在するか確認する
     *
     * @param email 確認するメールアドレス
     * @return 存在する場合 {@code true}
     */
    boolean existsByEmail(String email);
}
