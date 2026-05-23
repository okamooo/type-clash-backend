package com.example.typing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * パスワード再設定申請リクエスト DTO。
 *
 * @param email パスワード再設定対象のメールアドレス
 */
public record PasswordResetRequest(
        @NotBlank
        @Email
        String email) {
}
