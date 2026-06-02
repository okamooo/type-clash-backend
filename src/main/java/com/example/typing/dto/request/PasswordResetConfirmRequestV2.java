package com.example.typing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetConfirmRequestV2 {

    @NotBlank
    @Email(message = "{validation.email.format}")
    @Pattern(regexp = "^[^\\s　]*$", message = "{validation.email.no-space}")
    private String email;

    @NotBlank
    @Pattern(regexp = "^[^\\s　]*$", message = "{validation.password.no-space}")
    @Size(min = 8, max = 127, message = "{validation.password.size}")
    private String password;
}
