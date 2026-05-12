package com.example.typing.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.typing.dto.request.LoginRequest;
import com.example.typing.dto.response.ErrorResponse;
import com.example.typing.dto.response.LoginResponse;
import com.example.typing.entity.User;
import com.example.typing.security.JwtUtils;
import com.example.typing.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtils jwtUtils;
    private final AuthService authService;

    /**
     * 【ログアウト】
     * Cookieにセットしているトークンを削除する
     * @return
     */
    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
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
     * 成功した際にユーザー名を返却する
     * トークンはCookieにセット
     * 
     * @param request
     * @param result
     * @return
     */
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Validated @RequestBody LoginRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        try {
            User user = authService.authenticate(request);
            String token = jwtUtils.generateToken(user.getId());

            ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                    .httpOnly(true) // JavaScriptからアクセス不可にする
                    // .secure(true) // HTTPS通信のみで送信
                    .path("/") // 全てのパスで有効
                    .maxAge(60 * 60 * 24) // 有効期限（秒）
                    .sameSite("Lax") // CSRF対策
                    .build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new LoginResponse(user.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage(), LocalDateTime.now()));
        }

    }
}
