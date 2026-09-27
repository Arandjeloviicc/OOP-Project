package com.fittrack.backend.service.auth.register;

import com.fittrack.backend.repository.user.projection.CreatedUser;

public record RegistrationResult(
        RegisterStatus status,
        CreatedUser user
) {

    public static RegistrationResult success(CreatedUser user) {
        return new RegistrationResult(RegisterStatus.SUCCESS, user);
    }

    public static RegistrationResult usernameTaken() {
        return new RegistrationResult(RegisterStatus.USERNAME_TAKEN, null);
    }

    public static RegistrationResult emailTaken() {
        return new RegistrationResult(RegisterStatus.EMAIL_TAKEN, null);
    }
}