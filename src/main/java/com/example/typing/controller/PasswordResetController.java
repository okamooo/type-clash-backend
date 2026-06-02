package com.example.typing.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.typing.dto.request.PasswordResetConfirmRequestV2;
import com.example.typing.dto.request.PasswordResetRequest;
import com.example.typing.dto.request.OtpVerifyRequest;
import com.example.typing.service.PasswordResetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Value("${app.cookie.secure:false}")
    private boolean secureCookie;

    /**
     * 【パスワード再設定申請】
     * メールアドレスを受け取り、
     * OTP（ワンタイムパスワード）を発行してメール送信する。
     *
     * @param request パスワード再設定申請情報
     * @return 200 OK
     */
    @PostMapping("/request")
    public ResponseEntity<Void> requestReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestReset(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 【OTP検証】
     * 入力されたOTPを検証し、
     * パスワード再設定用JWTをCookieへ保存する。
     *
     * @param request OTP確認情報
     * @return 200 OK
     */
    @PostMapping("/verify")
    public ResponseEntity<Void> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        String token = passwordResetService.verifyOtp(request);

        ResponseCookie cookie = buildPasswordResetCookie(token, 60 * 10);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    /**
     * 【再設定セッション確認】
     * Cookieに保存されている
     * パスワード再設定用JWTの有効性を確認する。
     *
     * @param passwordResetToken パスワード再設定用JWT
     * @return 200 OK
     */
    @GetMapping("/verify-session")
    public ResponseEntity<Void> verifySession(
            @CookieValue(name = "passwordResetToken", required = false) String passwordResetToken) {

        passwordResetService.verifySession(passwordResetToken);

        return ResponseEntity.ok().build();
    }

    /**
     * 【パスワード再設定】
     * Cookie内のJWTを使用して
     * パスワードを更新する。
     * 更新後はCookieを削除する。
     *
     * @param passwordResetToken パスワード再設定用JWT
     * @param request 新しいパスワード情報
     * @return 200 OK
     */
    //　2段階認証を今回使用しないためコメントアウト対応
    // @PostMapping("/new")
    // public ResponseEntity<Void> resetPassword(
    //         @CookieValue(name = "passwordResetToken", required = false) String passwordResetToken,
    //         @Valid @RequestBody PasswordResetConfirmRequest request) {

    //     passwordResetService.resetPassword(passwordResetToken, request);

    //     ResponseCookie clearCookie = buildPasswordResetCookie("", 0);

    //     return ResponseEntity.ok()
    //             .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
    //             .build();
    // }

    /**
     * 【パスワード再設定】
     * パスワードを更新する。
     *
     * @param request 新しいパスワード情報
     * @return 200 OK
     */
    @PostMapping("/new")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody PasswordResetConfirmRequestV2 request) {

        passwordResetService.resetPassword(request);

        return ResponseEntity.ok().build();
    }

    /**
     * パスワード再設定用Cookieを生成する。
     *
     * @param value Cookieに保存するJWT
     * @param maxAge Cookieの有効期限（秒）
     * @return パスワード再設定用Cookie
     */

    private ResponseCookie buildPasswordResetCookie(String value, long maxAge) {

        return ResponseCookie.from("passwordResetToken", value)
                .httpOnly(true)
                // 本番で app.cookie.secure=true を設定
                // .secure(secureCookie)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
    }
}
