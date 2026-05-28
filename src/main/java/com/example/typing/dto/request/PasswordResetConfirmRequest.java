package com.example.typing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * パスワード再設定リクエスト DTO。
 *
 * @param password 新しいパスワード
 */
public record PasswordResetConfirmRequest(
        @NotBlank
        @Pattern(regexp = "^[^\\s　]*$", message = "{field.password}にスペースは使用できません")
        @Size(min = 8, max = 127, message = "{validation.password.size}")
        String password) {
}
