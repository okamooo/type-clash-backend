package com.example.typing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * OTP 検証リクエスト DTO。
 *
 * @param email メールアドレス
 * @param otp   ユーザーが入力した 6 桁のワンタイムパスワード
 */
public record OtpVerifyRequest(
        @NotBlank @Email @Pattern(regexp = "^[^\\s　]*$", message = "{validation.email.no-space}") String email,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "{validation.otp.size}") String otp) {
}
