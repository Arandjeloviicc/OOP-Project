package com.fittrack.backend.dto.auth;

public record LoginRequest(
        String email,
        String password
) {
}
