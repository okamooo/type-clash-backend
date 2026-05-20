package com.example.typing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * OTP 検証リクエスト DTO。
 *
 * @param email メールアドレス
 * @param otp   ユーザーが入力した 6 桁のワンタイムパスワード
 */
public record OtpVerifyRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 6,message = "6桁の認証コードを入力してください") String otp) {
}
