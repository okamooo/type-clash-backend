package com.example.typing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ユーザー登録リクエストDTO
@Data
public class UserRegisterRequest {

    @NotBlank(message = "{field.user-name}は必須です")
    @Pattern(regexp = "^[^\\s　]*$", message = "{field.user-name}にスペースは使用できません")
    @Size(max = 50, message = "{validation.user.name.size}")
    private String name;             // ユーザー名

    @NotBlank(message = "{field.email}は必須です")
    @Email(message = "{validation.email.format}")
    @Pattern(regexp = "^[^\\s　]*$", message = "{field.email}にスペースは使用できません")
    private String email;            // メールアドレス

    @NotBlank(message = "{field.password}は必須です")
    @Pattern(regexp = "^[^\\s　]*$", message = "{field.password}にスペースは使用できません")
    @Size(min = 8, max = 127, message = "{validation.password.size}")
    private String password;         // パスワード
}
