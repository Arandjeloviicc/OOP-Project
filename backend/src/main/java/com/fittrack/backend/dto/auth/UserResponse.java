package com.fittrack.backend.dto.auth;

public record UserResponse(
        Integer id,
        String username,
        String email
) {
}
