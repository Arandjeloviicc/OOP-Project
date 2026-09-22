package com.fittrack.api.auth.login;

import com.fittrack.model.user.User;

public record LoginResult(
        LoginStatus status,
        User user,
        boolean profileSetupComplete
) {
    public static LoginResult success(User user, boolean profileSetupComplete) {
        return new LoginResult(
                LoginStatus.SUCCESS,
                user,
                profileSetupComplete
        );
    }

    public static LoginResult userNotFound() {
        return new LoginResult(
                LoginStatus.USER_NOT_FOUND,
                null,
                false
        );
    }

    public static LoginResult wrongPassword() {
        return new LoginResult(
                LoginStatus.WRONG_PASSWORD,
                null,
                false
        );
    }
}