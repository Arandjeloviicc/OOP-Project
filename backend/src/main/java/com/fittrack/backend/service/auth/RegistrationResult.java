package com.fittrack.backend.service.auth;

import com.fittrack.backend.entity.user.User;

public record RegistrationResult(
        RegisterStatus status,
        User user
) {

    public static RegistrationResult success(User user) {
        return new RegistrationResult(RegisterStatus.SUCCESS, user);
    }

    public static RegistrationResult usernameTaken() {
        return new RegistrationResult(RegisterStatus.USERNAME_TAKEN, null);
    }

    public static RegistrationResult emailTaken() {
        return new RegistrationResult(RegisterStatus.EMAIL_TAKEN, null);
    }
}