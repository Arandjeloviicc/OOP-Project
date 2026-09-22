package com.fittrack.dto.auth.register;

import com.fittrack.dto.auth.UserResponse;

public record RegisterResponse(
        String status,
        UserResponse user
) {
}