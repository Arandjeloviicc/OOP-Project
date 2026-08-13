package com.fittrack.dto.auth;

public record LoginRequest(
        String email,
        String password
) {
}