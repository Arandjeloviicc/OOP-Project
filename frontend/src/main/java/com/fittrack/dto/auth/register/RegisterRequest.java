package com.fittrack.dto.auth.register;

public record RegisterRequest(
        String username,
        String email,
        String password
) {
}