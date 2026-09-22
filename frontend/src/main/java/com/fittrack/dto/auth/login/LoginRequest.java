package com.fittrack.dto.auth.login;

public record LoginRequest(
        String email,
        String password
) {
}