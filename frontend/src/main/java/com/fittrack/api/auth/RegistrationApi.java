package com.fittrack.api.auth;

import com.fittrack.api.common.BaseApi;
import com.fittrack.dto.auth.RegisterRequest;
import com.fittrack.dto.auth.RegisterResponse;
import com.fittrack.dto.auth.UserResponse;
import com.fittrack.model.user.User;

public class RegistrationApi extends BaseApi {

    private static final String API_URL = AUTH_URL + "/register";

    public RegistrationResult register(String username, String email, String password) {
        RegisterRequest request = new RegisterRequest(username, email, password);

        RegisterResponse response = apiClient.post(
                API_URL,
                request,
                200,
                RegisterResponse.class
        );

        return switch (response.status()) {
            case "SUCCESS" -> {
                UserResponse responseUser = response.user();

                User user = new User(
                        responseUser.id(),
                        responseUser.username(),
                        responseUser.email()
                );

                yield RegistrationResult.success(user);
            }

            case "USERNAME_TAKEN" -> RegistrationResult.usernameTaken();
            case "EMAIL_TAKEN" -> RegistrationResult.emailTaken();

            default -> throw new IllegalStateException("Unexpected registration status: " + response.status());
        };
    }
}