package com.fittrack.backend.dto.auth;

public record RegisterRequest(
        String username,
        String email,
        String password
) {
}
