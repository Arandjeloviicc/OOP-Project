package com.fittrack.dto.auth.login;

import com.fittrack.dto.auth.UserResponse;

public record LoginResponse(
        String status,
        UserResponse user,
        boolean profileSetupComplete
) {}