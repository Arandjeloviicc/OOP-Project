package com.fittrack.backend.repository.user.projection;

public record CreatedUser(
        Integer id,
        String username,
        String email
) {}