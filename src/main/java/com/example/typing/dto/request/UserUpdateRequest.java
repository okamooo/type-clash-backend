package com.example.typing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ユーザー情報更新リクエストDTO（nullの場合は更新しない）
@Data
public class UserUpdateRequest {

    @Size(max = 50, message = "{validation.user.name.size}")
    @Pattern(regexp = "^[^\\s　]*$", message = "{field.user-name}にスペースは使用できません")
    private String name;             // ユーザー名

    @Email(message = "{validation.email.format}")
    @Pattern(regexp = "^[^\\s　]*$", message = "{field.email}にスペースは使用できません")
    private String email;            // メールアドレス

    @Size(max = 127, message = "{validation.current-password.size}")
    @Pattern(regexp = "^[^\\s　]*$", message = "{field.current-password}にスペースは使用できません")
    private String currentPassword;  // 現在のパスワード（パスワード変更時に必須）

    @Size(min = 8, max = 127, message = "{validation.password.size}")
    @Pattern(regexp = "^[^\\s　]*$", message = "{field.password}にスペースは使用できません")
    private String password;         // 新しいパスワード

    private String iconImage;        // アイコン画像URL
    private Integer backgroundImage; // 背景画像ID（0 or 1）
}
