package com.fittrack.backend.service.auth;

public record LoginResult(
        LoginStatus status,
        Integer userId,
        String username,
        String email,
        boolean profileSetupComplete
) {

    public static LoginResult success(
            Integer userId,
            String username,
            String email,
            boolean profileSetupComplete
    ) {
        return new LoginResult(
                LoginStatus.SUCCESS,
                userId,
                username,
                email,
                profileSetupComplete
        );
    }

    public static LoginResult userNotFound() {
        return new LoginResult(
                LoginStatus.USER_NOT_FOUND,
                null,
                null,
                null,
                false
        );
    }

    public static LoginResult wrongPassword() {
        return new LoginResult(
                LoginStatus.WRONG_PASSWORD,
                null,
                null,
                null,
                false
        );
    }
}
