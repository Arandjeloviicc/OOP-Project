package com.fittrack.api.auth;

import com.fittrack.model.user.User;

public record RegistrationResult(RegistrationStatus status, User user) {
    public static RegistrationResult success(User user) {
        return new RegistrationResult(RegistrationStatus.SUCCESS, user);
    }

    public static RegistrationResult usernameTaken() {
        return new RegistrationResult(RegistrationStatus.USERNAME_TAKEN, null);
    }

    public static RegistrationResult emailTaken() {
        return new RegistrationResult(RegistrationStatus.EMAIL_TAKEN, null);
    }
}