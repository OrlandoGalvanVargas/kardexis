package com.kardexis.auth.api.dto;

import com.kardexis.auth.domain.User;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String username,
        String email,
        String displayName
) {
    public static UserSummary fromEntity(User user) {
        return new UserSummary(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName()
        );
    }
}