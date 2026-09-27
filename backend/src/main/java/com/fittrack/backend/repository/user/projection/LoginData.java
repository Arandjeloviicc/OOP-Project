package com.fittrack.backend.repository.user.projection;

public record LoginData(
        Integer id,
        String username,
        String email,
        String passwordHash,
        boolean profileSetupComplete
) {}