package com.fittrack.dto.auth;

public record LoginResponse(
        String status,
        UserResponse user
) {
}