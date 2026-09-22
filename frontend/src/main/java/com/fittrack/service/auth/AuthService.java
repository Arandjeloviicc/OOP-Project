package com.fittrack.service.auth;

import com.fittrack.api.auth.login.LoginApi;
import com.fittrack.api.auth.login.LoginResult;
import com.fittrack.api.auth.login.LoginStatus;
import com.fittrack.api.auth.register.RegistrationApi;
import com.fittrack.api.auth.register.RegistrationResult;
import com.fittrack.api.auth.register.RegistrationStatus;
import com.fittrack.model.user.User;
import com.fittrack.session.UserSession;

public class AuthService {

    private final LoginApi loginApi;
    private final RegistrationApi registrationApi;
    private final UserSession userSession;

    public AuthService() {
        this.loginApi = new LoginApi();
        this.registrationApi = new RegistrationApi();
        this.userSession = UserSession.getInstance();
    }

    // ── Login ─────────────────────────────────────────────
    public LoginResult login(String email, String password) {
        LoginResult result = loginApi.login(
                email,
                password
        );

        if (result.status() == LoginStatus.SUCCESS) {
            userSession.start(result.user());
        }

        return result;
    }

    // ── Register ─────────────────────────────────────────────
    public RegistrationResult register(String username, String email, String password) {
        RegistrationResult result = registrationApi.register(
                username,
                email,
                password
        );

        if (result.status() == RegistrationStatus.SUCCESS) {
            userSession.start(result.user());
        }

        return result;
    }

    // ── UserSession ─────────────────────────────────────────────
    public User getCurrentUser() {
        return userSession.requireCurrentUser();
    }

    public void logout() {
        userSession.end();
    }
}
