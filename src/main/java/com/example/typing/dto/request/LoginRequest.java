package com.example.typing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "{field.email}は必須です")
    @Email(message = "{validation.email.format}")
    @Pattern(regexp = "^[^\\s　]*$", message = "{field.email}にスペースは使用できません")
    private String email; // メールアドレス

    @Size(min = 8, max = 127, message = "{validation.password.size}")
    @NotBlank(message = "{field.password}は必須です")
    @Pattern(regexp = "^[^\\s　]*$", message = "{field.password}にスペースは使用できません")
    private String password; // パスワード
}
