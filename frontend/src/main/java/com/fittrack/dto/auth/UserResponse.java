package com.fittrack.dto.auth;

public record UserResponse(
        int id,
        String username,
        String email
) {
}
