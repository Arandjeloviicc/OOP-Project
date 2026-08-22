package com.fittrack.api.nutrition;

import com.fittrack.api.common.BaseApi;
import com.fittrack.dto.nutrition.food.CreateFoodRequest;
import com.fittrack.dto.nutrition.food.FoodResponse;
import tools.jackson.core.type.TypeReference;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FoodApi extends BaseApi {

    private static final String API_URL = NUTRITION_URL + "/foods";

    public List<FoodResponse> searchFoods(String search) {
        try {
            String encodedSearch =
                    URLEncoder.encode(
                            search,
                            StandardCharsets.UTF_8
                    );

            String url = API_URL + "?search=" + encodedSearch;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Failed to load foods."
                );
            }

            return objectMapper.readValue(
                    response.body(),
                    new TypeReference<>() {}
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not communicate with the FitTrack server.",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Food request was interrupted.",
                    exception
            );
        }
    }

    public List<FoodResponse> getMyFoods(Integer userId, String search) {
        try {
            String encodedSearch = URLEncoder.encode(
                    search == null ? "" : search,
                    StandardCharsets.UTF_8
            );

            String url = API_URL + "/mine/" + userId + "?search=" + encodedSearch;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Failed to load user foods."
                );
            }

            return objectMapper.readValue(
                    response.body(),
                    new TypeReference<>() {}
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not communicate with the FitTrack server.",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Food request was interrupted.",
                    exception
            );
        }
    }

    public FoodResponse createFood(Integer userId, CreateFoodRequest requestData) {
        try {
            String url = API_URL + "/user/" + userId;

            String requestBody = objectMapper.writeValueAsString(requestData);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 201) {
                throw new IllegalStateException(
                        "Failed to create food."
                );
            }

            return objectMapper.readValue(
                    response.body(),
                    FoodResponse.class
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not communicate with the FitTrack server.",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Food creation request was interrupted.",
                    exception
            );
        }
    }
}
