package com.example.typing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ユーザー情報更新リクエストDTO（nullの場合は更新しない）
@Data
public class UserUpdateRequest {

    @Size(max = 50, message = "ユーザー名は50文字以内で入力してください")
    private String name;             // ユーザー名

    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;            // メールアドレス

    private String currentPassword;  // 現在のパスワード（パスワード変更時に必須）

    @Size(min = 8, max = 127, message = "パスワードは8文字以上127文字以内で入力してください")
    private String password;         // 新しいパスワード

    private String iconImage;        // アイコン画像URL
    private Integer backgroundImage; // 背景画像ID（0 or 1）
}
