package com.example.typing.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.typing.dto.request.OtpVerifyRequest;
import com.example.typing.dto.request.PasswordResetConfirmRequestV2;
import com.example.typing.dto.request.PasswordResetRequest;
import com.example.typing.entity.OtpToken;
import com.example.typing.entity.User;
import com.example.typing.exception.OtpAuthenticationException;
import com.example.typing.exception.UserNotFoundException;
import com.example.typing.repository.OtpTokenRepository;
import com.example.typing.repository.UserRepository;
import com.example.typing.security.JwtUtils;

import io.jsonwebtoken.JwtException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_FAIL_COUNT = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final SendMailService sendMailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * 【パスワード再設定申請】
     * メールアドレスをもとにユーザーを確認し、
     * 6桁の認証コードを発行してメール送信する。
     *
     * @param request パスワード再設定申請情報
     */
    public void requestReset(PasswordResetRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new UserNotFoundException(request.email()));

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));

        Optional<OtpToken> existingToken = otpTokenRepository.findByEmail(request.email());
        OtpToken token;
        if (existingToken.isPresent()) {
            // データがある場合：既存のデータを使い回す（JPAが自動でUPDATEと判断する）
            token = existingToken.get();
        } else {
            // データがない場合：完全に新しく作る（JPAが自動でINSERTと判断する）
            token = new OtpToken();
        }

        token.setEmail(request.email());
        token.setOtpHash(passwordEncoder.encode(otp));
        token.setExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        token.setFailCount(0);

        try {
            sendMailService.sendTemplateMail(
                    request.email(),
                    "【Type Clash】パスワード再設定コード",
                    "otp",
                    Map.of("userName", user.getName(), "otp", otp, "expiryMinutes", OTP_EXPIRY_MINUTES));
            otpTokenRepository.save(token);
        } catch (MessagingException e) {
            throw new RuntimeException("メール送信に失敗しました", e);
        }
    }

    /**
     * 【パスワード再設定用OTP検証】
     * 入力された認証コードを検証し、
     * 正常であればパスワード再設定用JWTを発行する。
     *
     * @param request OTP検証情報
     * @return パスワード再設定用JWT
     */
    @Transactional(noRollbackFor = OtpAuthenticationException.class)
    public String verifyOtp(OtpVerifyRequest request) {
        OtpToken token = otpTokenRepository.findByEmail(request.email())
                .orElseThrow(() -> new OtpAuthenticationException("認証に失敗しました。"));

        if (LocalDateTime.now().isAfter(token.getExpiry())) {
            otpTokenRepository.delete(token);
            throw new OtpAuthenticationException("認証に失敗しました。");
        }

        if (!passwordEncoder.matches(request.otp(), token.getOtpHash())) {
            token.setFailCount(token.getFailCount() + 1);

            if (token.getFailCount() >= MAX_FAIL_COUNT) {
                otpTokenRepository.delete(token);
                throw new OtpAuthenticationException("試行回数の上限に達しました。再度認証コードを発行してください。");
            }

            throw new OtpAuthenticationException("認証に失敗しました。");
        }

        return jwtUtils.generatePasswordResetToken(token.getEmail());
    }

    /**
     * 【パスワード再設定セッション確認】
     * Cookieから取得したJWTを検証し、
     * パスワード再設定可能な状態か確認する。
     *
     * @param passwordResetToken パスワード再設定用JWT
     */
    @Transactional(readOnly = true)
    public void verifySession(String passwordResetToken) {
        getEmailFromPasswordResetToken(passwordResetToken);
    }

    /**
     * 【パスワード再設定】
     * Cookie内のJWTから対象ユーザーを特定し、
     * 新しいパスワードへ更新する。
     *
     * @param passwordResetToken パスワード再設定用JWT
     * @param request            新しいパスワード情報
     */
    // ２段階認証を使用しないため、コメントアウト
    // public void resetPassword(String passwordResetToken, PasswordResetConfirmRequest request) {
    //     String email = getEmailFromPasswordResetToken(passwordResetToken);

    //     User user = userRepository.findByEmailAndDeletedAtIsNull(email)
    //             .orElseThrow(() -> new OtpAuthenticationException("有効なユーザーが見つかりません"));

    //     user.setPassword(passwordEncoder.encode(request.password()));

    //     otpTokenRepository.deleteByEmail(email);
    // }

    public void resetPassword(PasswordResetConfirmRequestV2 request) {

        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new OtpAuthenticationException("有効なユーザーが見つかりません"));

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        otpTokenRepository.deleteByEmail(request.getEmail());
    }

    /**
     * 【パスワード再設定用JWTからメールアドレスを取得】
     * JWTの存在確認・署名・有効期限・scopeを検証し、
     * subjectに設定されたメールアドレスを取得する。
     *
     * @param passwordResetToken パスワード再設定用JWT
     * @return パスワード再設定対象のメールアドレス
     */
    private String getEmailFromPasswordResetToken(String passwordResetToken) {
        if (passwordResetToken == null || passwordResetToken.isBlank()) {
            throw new OtpAuthenticationException("無効なトークンです。");
        }

        try {
            return jwtUtils.getEmailFromPasswordResetToken(passwordResetToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new OtpAuthenticationException("無効なトークンです。");
        }
    }
}
