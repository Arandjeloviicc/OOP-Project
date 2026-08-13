package com.fittrack.api.auth;

import com.fittrack.api.JsonMapper;
import com.fittrack.dto.auth.RegisterRequest;
import com.fittrack.dto.auth.RegisterResponse;
import com.fittrack.dto.auth.UserResponse;
import com.fittrack.model.user.User;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RegistrationApi {

    private static final String BASE_URL = "http://localhost:8080/api/auth";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = JsonMapper.getMapper();

    public RegistrationApi() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public RegistrationResult register(String username, String email, String password) {

        RegisterRequest registerRequest = new RegisterRequest(username, email, password);

        String requestBody = objectMapper.writeValueAsString(registerRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Registration request failed with status: " + response.statusCode()
                );
            }

            RegisterResponse registerResponse = objectMapper.readValue(response.body(), RegisterResponse.class);

            return switch (registerResponse.status()) {
                case "SUCCESS" -> {
                    UserResponse responseUser = registerResponse.user();

                    User user = new User(
                            responseUser.id(),
                            responseUser.username(),
                            responseUser.email()
                    );

                    yield RegistrationResult.success(user);
                }

                case "USERNAME_TAKEN" -> RegistrationResult.usernameTaken();
                case "EMAIL_TAKEN" -> RegistrationResult.emailTaken();

                default -> throw new IllegalStateException("Unexpected value: " + registerResponse.status());
            };

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not connect to the FitTrack server.",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Registration request was interrupted.",
                    exception
            );
        }
    }
}