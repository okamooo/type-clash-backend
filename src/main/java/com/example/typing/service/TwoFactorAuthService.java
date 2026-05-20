package com.example.typing.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.typing.dto.request.UserRegisterRequest;
import com.example.typing.entity.OtpToken;
import com.example.typing.exception.EmailAlreadyExistsException;
import com.example.typing.exception.OtpAuthenticationException;
import com.example.typing.repository.OtpTokenRepository;
import com.example.typing.repository.UserRepository;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TwoFactorAuthService {

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_FAIL_COUNT = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final SendMailService sendMailService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    /**
     * 【一時ユーザー情報テーブルの作成】
     * 一時ユーザーテーブルを作成し、ワンタイムパスワードを発行する
     * 
     * @param request
     */
    public void registOtpToken(UserRegisterRequest request) {
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        String hashedOtp = passwordEncoder.encode(otp);

        // Userテーブルのメールアドレスの重複チェック
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        Optional<OtpToken> existingToken = otpTokenRepository.findByEmail(request.getEmail());

        OtpToken token;
        if (existingToken.isPresent()) {
            //データがある場合：既存のデータを使い回す（JPAが自動でUPDATEと判断する）
            token = existingToken.get();
        } else {
            //データがない場合：完全に新しく作る（JPAが自動でINSERTと判断する）
            token = new OtpToken();
            token.setEmail(request.getEmail());
        }

        token.setName(request.getName());
        token.setPassword(passwordEncoder.encode(request.getPassword()));
        token.setOtpHash(hashedOtp);
        token.setExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        token.setFailCount(0);

        try {
            otpTokenRepository.save(token);
            sendOtp(request.getEmail(), request.getName(), otp);
        } catch (MessagingException e) {
            throw new RuntimeException("メール送信に失敗したため、登録処理を中断しました");
        } catch (Exception e) {
            throw new RuntimeException("トークンの保存に失敗しました");
        }
    }

    /**
     * 【OTP送信】
     * 指定したメールアドレスにワンタイムパスワードを送信する
     *
     * @param email 送信先メールアドレス
     * @param name  宛先ユーザー名（メール本文内の表示用）
     * @param otp   送信するワンタイムパスワード（平文・6桁）
     * @throws MessagingException メール送信に失敗した場合
     */
    private void sendOtp(String email, String name, String otp) throws MessagingException {

        sendMailService.sendTemplateMail(
                email,
                "【Type Clash】認証コード",
                "otp",
                Map.of("userName", name, "otp", otp, "expiryMinutes", OTP_EXPIRY_MINUTES));
    }

    /**
     * 【OTP認証】
     * 送られてきたワンタイムパスワードを確認し、
     * 正常であればUserテーブルに本登録する。
     * 
     * @param email
     * @param rawOtp
     */

    @Transactional(noRollbackFor = OtpAuthenticationException.class)
    public void verifyOtp(String email, String rawOtp) {

        // メールアドレスの有無を確認
        OtpToken token = otpTokenRepository.findByEmail(email)
                .orElseThrow(() -> new OtpAuthenticationException("認証に失敗しました。"));

        // 有効期限が切れていないか確認
        if (LocalDateTime.now().isAfter(token.getExpiry())) {
            throw new OtpAuthenticationException("認証に失敗しました。");
        }

        // ワンタイムパスワードがマッチしているか確認
        if (!passwordEncoder.matches(rawOtp, token.getOtpHash())) {
            token.setFailCount(token.getFailCount() + 1);
            if (token.getFailCount() >= MAX_FAIL_COUNT) {
                otpTokenRepository.delete(token);
                throw new OtpAuthenticationException("試行回数の上限に達しました。再度ワンタイムパスワードを発行してください。");
            }
            throw new OtpAuthenticationException("認証に失敗しました。");
        }

        // userテーブルに本登録
        userService.registerUser(token);

        // ユーザー一時情報を削除
        otpTokenRepository.delete(token);
    }

}
