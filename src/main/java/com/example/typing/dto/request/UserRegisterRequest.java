package com.example.typing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ユーザー登録リクエストDTO
@Data
public class UserRegisterRequest {

    @NotBlank(message = "ユーザー名は必須です")
    @Size(max = 50, message = "ユーザー名は50文字以内で入力してください")
    private String name;             // ユーザー名

    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;            // メールアドレス

    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, max = 127, message = "パスワードは8文字以上127文字以内で入力してください")
    private String password;         // パスワード

    private String iconImage;        // アイコン画像URL
    private Integer backgroundImage; // 背景画像ID（0 or 1）
}
