package com.fittrack.api.auth.login;

import com.fittrack.api.common.BaseApi;
import com.fittrack.dto.auth.login.LoginRequest;
import com.fittrack.dto.auth.login.LoginResponse;
import com.fittrack.dto.auth.UserResponse;
import com.fittrack.model.user.User;

public class LoginApi extends BaseApi {

    private static final String API_URL = AUTH_URL + "/login";

    public LoginResult login(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);

        LoginResponse response = apiClient.post(
                API_URL,
                request,
                200,
                LoginResponse.class
        );

        return switch (response.status()) {
            case "SUCCESS" -> {
                UserResponse responseUser = response.user();

                User user = new User(
                        responseUser.id(),
                        responseUser.username(),
                        responseUser.email()
                );

                yield LoginResult.success(
                        user,
                        response.profileSetupComplete()
                );
            }

            case "USER_NOT_FOUND" -> LoginResult.userNotFound();
            case "WRONG_PASSWORD" -> LoginResult.wrongPassword();

            default -> throw new IllegalStateException("Unexpected login status: " + response.status());
        };
    }
}