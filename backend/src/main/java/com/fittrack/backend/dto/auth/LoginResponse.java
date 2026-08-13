package com.fittrack.backend.dto.auth;

public record LoginResponse(
        String status,
        UserResponse user
) {
}
