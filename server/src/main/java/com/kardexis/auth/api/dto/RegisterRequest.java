package com.kardexis.auth.api.dto;

import com.kardexis.shared.validation.MaxUtf8Bytes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(
                min = 3,
                max = 50,
                message = "Username must be between 3 and 50 characters"
        )
        String username,

        @NotBlank
        @Email(message = "Invalid email address format")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 72,
                message = "Password must be between 8 and 72 characters"
        )
        @MaxUtf8Bytes(
                value = 72,
                message = "Password must not exceed 72 bytes when encoded as UTF-8"
        )
        String password,

        @Size(max = 100)
        String displayName
) {
    public RegisterRequest {
        username = username != null ? username.trim() : null;
        email = email != null ? email.trim().toLowerCase() : null;
        displayName = displayName != null ? displayName.trim() : null;
    }
}