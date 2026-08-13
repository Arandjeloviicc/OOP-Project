package com.fittrack.dto.auth;

public record RegisterResponse(
        String status,
        UserResponse user
) {
}