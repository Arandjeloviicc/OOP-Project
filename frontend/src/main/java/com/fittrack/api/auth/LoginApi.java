package com.fittrack.api.auth;

import com.fittrack.api.JsonMapper;
import com.fittrack.dto.auth.LoginRequest;
import com.fittrack.dto.auth.LoginResponse;
import com.fittrack.dto.auth.UserResponse;
import com.fittrack.model.user.User;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoginApi {

    private static final String BASE_URL = "http://localhost:8080/api/auth";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = JsonMapper.getMapper();

    public LoginResult login(String email, String password) {

        LoginRequest loginRequest = new LoginRequest(email, password);

        String requestBody = objectMapper.writeValueAsString(loginRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
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
                        "Login request failed with status: " + response.statusCode()
                );
            }

            LoginResponse loginResponse = objectMapper.readValue(response.body(), LoginResponse.class);

            return switch (loginResponse.status()) {
                case "SUCCESS" -> {
                    UserResponse responseUser = loginResponse.user();

                    User user = new User(
                            responseUser.id(),
                            responseUser.username(),
                            responseUser.email()
                    );

                    yield LoginResult.success(user);
                }

                case "USER_NOT_FOUND" -> LoginResult.userNotFound();
                case "WRONG_PASSWORD" -> LoginResult.wrongPassword();

                default -> throw new IllegalStateException("Unexpected value: " + loginResponse.status());
            };
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not communicate with the FitTrack server.",
                    exception
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Login request was interrupted.",
                    exception
            );
        }
    }
}