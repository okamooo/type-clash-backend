package com.example.typing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    private String email; // メールアドレス

    @Size(min = 8, max = 127, message = "パスワードは8文字以上127文字以内で入力してください")
    @NotBlank(message = "パスワードは必須です")
    private String password; // パスワード
}
