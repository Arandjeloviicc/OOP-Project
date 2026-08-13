package com.fittrack.backend.dto.auth;

public record RegisterResponse(
        String status,
        UserResponse user
) {
}
