package com.example.typing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * パスワード再設定リクエスト DTO。
 *
 * @param password 新しいパスワード
 */
public record PasswordResetConfirmRequest(
        @NotBlank
        @Size(min = 8, max = 127, message = "パスワードは8文字以上、127文字以内で入力してください")
        String password) {
}
