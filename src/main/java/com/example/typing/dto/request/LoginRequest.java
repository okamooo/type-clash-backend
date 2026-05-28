package com.example.typing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    @Pattern(regexp = "^[^\\s　]*$", message = "{validation.email.no-space}")
    private String email; // メールアドレス

    @Size(min = 8, max = 127, message = "{validation.password.size}")
    @NotBlank(message = "{validation.password.required}")
    @Pattern(regexp = "^[^\\s　]*$", message = "{validation.password.no-space}")
    private String password; // パスワード
}
