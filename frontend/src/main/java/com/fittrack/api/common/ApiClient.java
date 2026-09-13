package com.fittrack.api.common;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class ApiClient {

    private static final ApiClient INSTANCE = new ApiClient();

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        this.objectMapper = JsonMapper.getMapper();
    }

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    // ── GET ─────────────────────────────────────────────────────
    public <T> T get(String url, int expectedStatus, Class<T> responseType) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = send(request, expectedStatus);

        return readResponse(
                response.body(),
                responseType
        );
    }

    public <T> T get(String url, int expectedStatus, TypeReference<T> responseType) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = send(request, expectedStatus);

        return readResponse(
                response.body(),
                responseType
        );
    }

    // ── POST ────────────────────────────────────────────────────
    public void post(String url, Object requestBody, int expectedStatus) {
        HttpRequest request = createJsonRequest(
                url,
                "POST",
                requestBody
        );

        send(request, expectedStatus);
    }

    public <T> T post(String url, Object requestBody, int expectedStatus, Class<T> responseType) {
        HttpRequest request = createJsonRequest(
                url,
                "POST",
                requestBody
        );

        HttpResponse<String> response = send(request, expectedStatus);

        return readResponse(
                response.body(),
                responseType
        );
    }

    // ── PUT ─────────────────────────────────────────────────────
    public void put(String url, Object requestBody, int expectedStatus) {
        HttpRequest request = createJsonRequest(
                url,
                "PUT",
                requestBody
        );

        send(request, expectedStatus);
    }

    public <T> T put(String url, Object requestBody, int expectedStatus, Class<T> responseType) {
        HttpRequest request = createJsonRequest(
                url,
                "PUT",
                requestBody
        );

        HttpResponse<String> response = send(request, expectedStatus);

        return readResponse(
                response.body(),
                responseType
        );
    }

    // ── DELETE ──────────────────────────────────────────────────
    public void delete(String url, int expectedStatus) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .DELETE()
                .build();

        send(request, expectedStatus);
    }

    // ── Helpers ─────────────────────────────────────────────────
    private HttpRequest createJsonRequest(String url, String method, Object requestBody) {
        String json = writeRequest(requestBody);

        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .method(
                        method,
                        HttpRequest.BodyPublishers.ofString(json)
                )
                .build();
    }

    private String writeRequest(Object requestBody) {
        return objectMapper.writeValueAsString(requestBody);
    }

    private HttpResponse<String> send(HttpRequest request, int expectedStatus) {
        try {
            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != expectedStatus) {
                throw new ApiException(
                        response.statusCode(),
                        response.body()
                );
            }

            return response;

        } catch (IOException exception) {
            throw new ApiException(
                    "Could not communicate with the FitTrack server.",
                    exception
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new ApiException(
                    "Request was interrupted.",
                    exception
            );
        }
    }

    private <T> T readResponse(String responseBody, Class<T> responseType) {
        return objectMapper.readValue(
                responseBody,
                responseType
        );
    }

    private <T> T readResponse(String responseBody, TypeReference<T> responseType) {
        return objectMapper.readValue(
                responseBody,
                responseType
        );
    }
}