package com.kardexis.shared.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank
        @Size(min = 32, message = "JWT secret key must be at least 32 characters long for HMAC-SHA256")
        String secret,

        @Positive
        int accessTokenMinutes,

        @Positive
        int refreshTokenDays
) {
}