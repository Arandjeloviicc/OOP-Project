package com.fittrack.service.auth;

import com.fittrack.model.user.User;

public record AuthenticationResult(AuthenticationStatus status, User user) {
    public static AuthenticationResult success(User user) {
        return new AuthenticationResult(AuthenticationStatus.SUCCESS, user);
    }

    public static AuthenticationResult userNotFound() {
        return new AuthenticationResult(AuthenticationStatus.USER_NOT_FOUND, null);
    }

    public static AuthenticationResult wrongPassword() {
        return new AuthenticationResult(AuthenticationStatus.WRONG_PASSWORD, null);
    }
}
