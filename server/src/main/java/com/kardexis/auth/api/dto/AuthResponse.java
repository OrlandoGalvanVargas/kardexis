package com.kardexis.auth.api.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UserSummary user
) {
    public AuthResponse(String accessToken, UserSummary user) {
        this(accessToken, "Bearer", user);
    }
}