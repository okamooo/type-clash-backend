package com.example.typing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    @Pattern(regexp = "^[^\\s　]*$", message = "{validation.email.no-space}")
    private String email; // メールアドレス

    @NotBlank(message = "{validation.password.required}")
    private String password; // パスワード
}
