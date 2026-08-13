package com.fittrack.dto.auth;

public record RegisterRequest(
        String username,
        String email,
        String password
) {
}