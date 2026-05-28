package com.example.typing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ユーザー登録リクエストDTO
@Data
public class UserRegisterRequest {

    @NotBlank(message = "{validation.user.name.required}")
    @Pattern(regexp = "^[^\\s　]+$", message = "{validation.user.name.no-space}")
    @Size(max = 50, message = "{validation.user.name.size}")
    private String name;             // ユーザー名

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    @Pattern(regexp = "^[^\\s　]+$", message = "{validation.email.no-space}")
    private String email;            // メールアドレス

    @NotBlank(message = "{validation.password.required}")
    @Pattern(regexp = "^[^\\s　]+$", message = "{validation.password.no-space}")
    @Size(min = 8, max = 127, message = "{validation.password.size}")
    private String password;         // パスワード
}
