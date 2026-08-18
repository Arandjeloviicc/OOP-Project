package com.fittrack.api.nutrition;

import com.fittrack.api.common.BaseApi;
import com.fittrack.dto.nutrition.AddMealItemRequest;
import com.fittrack.dto.nutrition.MealItemResponse;
import com.fittrack.dto.nutrition.MealResponse;
import tools.jackson.core.type.TypeReference;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

public class MealApi extends BaseApi {

    private static final String API_URL = NUTRITION_URL + "/meals";

    public List<MealResponse> getMealsFromDate(Integer userId, LocalDate mealDate) {
        try {
            String url = API_URL + "/user/" + userId + "?date=" + mealDate;

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
                        "Failed to load meals."
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
                    "Meal request was interrupted.",
                    exception
            );
        }
    }

    public MealItemResponse addMealItem(Integer userId, AddMealItemRequest requestData) {
        try {
            String url = API_URL + "/user/" + userId + "/items";

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
                        "Failed to add meal item."
                );
            }

            return objectMapper.readValue(
                    response.body(),
                    MealItemResponse.class
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not communicate with the FitTrack server.",
                    exception
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Meal item request was interrupted.",
                    exception
            );
        }
    }
}