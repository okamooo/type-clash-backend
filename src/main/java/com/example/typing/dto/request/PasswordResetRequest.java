package com.example.typing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * パスワード再設定申請リクエスト DTO。
 *
 * @param email パスワード再設定対象のメールアドレス
 */
public record PasswordResetRequest(
        @NotBlank
        @Email
        @Pattern(regexp = "^[^\\s　]*$", message = "{field.email}にスペースは使用できません")
        String email) {
}
