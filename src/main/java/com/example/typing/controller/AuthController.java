package com.example.typing.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.typing.dto.request.LoginRequest;
import com.example.typing.dto.request.OtpVerifyRequest;
import com.example.typing.dto.request.UserRegisterRequest;
import com.example.typing.dto.response.LoginResponse;
import com.example.typing.dto.response.UserResponse;
import com.example.typing.entity.User;
import com.example.typing.security.JwtUtils;
import com.example.typing.service.AuthService;
import com.example.typing.service.LoginSessionService;
import com.example.typing.service.TwoFactorAuthService;
import com.example.typing.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
        private final JwtUtils jwtUtils;
        private final AuthService authService;
        private final LoginSessionService loginSessionService;
        private final TwoFactorAuthService twoFactorAuthService;
        private final UserService userService;

        /**
         * 【OTP検証】
         * ワンタイムパスワードを検証する
         * 登録用のJWTを発行
         */
        @PostMapping("/auth/otp/verify")
        public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
                String registerToken = twoFactorAuthService.verifyOtp(request);
                ResponseCookie cookie = ResponseCookie.from("registerToken", registerToken)
                                .httpOnly(true)
                                .path("/")
                                .maxAge(60 * 10)
                                .sameSite("Lax")
                                .build();
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .build();
        }

        /**
         * 【OTPとユーザー情報の一時保存】
         * OTPとユーザー情報をデータベースに保存後
         * ワンタイムパスワードをメールで送る
         * 
         * @param request
         * @return
         */

        @PostMapping("/auth/otp/register")
        public ResponseEntity<?> registerOtp(@Valid @RequestBody UserRegisterRequest request) {
                twoFactorAuthService.registOtpToken(request);
                return ResponseEntity.ok().build();
        }

        /**
         * 【ユーザーテーブルへの本登録】
         * 2段階認証を今回行わずに登録するため、コメントアウト
         * @param request
         * @return
         */
        // @PostMapping("/auth/registerUser")
        // public ResponseEntity<?> registerUser(@CookieValue String registerToken) {
        // twoFactorAuthService.registUser(registerToken);
        // ResponseCookie clearCookie = ResponseCookie.from("registerToken", "")
        // .httpOnly(true)
        // .path("/")
        // .maxAge(0)
        // .sameSite("Lax")
        // .build();
        // return ResponseEntity.ok()
        // .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
        // .build();
        // }

        @PostMapping("/auth/registerUser")
        public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegisterRequest request) {
                UserResponse response = userService.registerUser(request);
                return ResponseEntity.status(201).body(response);
        }

        /**
         * 【ログアウト】
         * Cookieにセットしているトークンを削除する
         * 
         * @return
         */
        @PostMapping("/auth/logout")
        public ResponseEntity<?> logout(@CookieValue(name = "accessToken", required = false) String accessToken) {
                if (accessToken != null && jwtUtils.validateToken(accessToken)) {
                        Long userId = jwtUtils.getUserIdFromToken(accessToken);
                        String loginSessionId = jwtUtils.getLoginSessionIdFromToken(accessToken);
                        loginSessionService.deleteSessionIfMatches(userId, loginSessionId);
                }
                ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                                .httpOnly(true)
                                // .secure(true) //HTTPS通信のみで送信
                                .path("/")
                                .maxAge(0)
                                .sameSite("Lax")
                                .build();
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .build();
        }

        /**
         * 【ログイン機能】
         * メールアドレスとパスワードからログインを行い、
         * 成功した際にユーザー名を返却する。
         * トークンはCookieにセットする。
         *
         * @param request ログインリクエスト（メールアドレス・パスワード）
         * @return ログイン成功時はユーザー情報、失敗時は 401
         */
        @PostMapping("/auth/login")
        public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

                User user = authService.authenticate(request);
                String loginSessionId = loginSessionService.createSession(user.getId());
                String token = jwtUtils.generateToken(user.getId(), loginSessionId);

                ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                                .httpOnly(true)
                                .path("/")
                                .maxAge(60 * 60 * 24)
                                .sameSite("Lax")
                                .build();
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .body(new LoginResponse(user.getId(), user.getName()));

        }
}
