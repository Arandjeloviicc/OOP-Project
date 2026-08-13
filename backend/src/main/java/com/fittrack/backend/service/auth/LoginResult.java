package com.fittrack.backend.service.auth;

import com.fittrack.backend.entity.user.User;

public record LoginResult(
        LoginStatus status,
        User user
) {

    public static LoginResult success(User user) {
        return new LoginResult(LoginStatus.SUCCESS, user);
    }

    public static LoginResult userNotFound() {
        return new LoginResult(LoginStatus.USER_NOT_FOUND, null);
    }

    public static LoginResult wrongPassword() {
        return new LoginResult(LoginStatus.WRONG_PASSWORD, null);
    }
}
