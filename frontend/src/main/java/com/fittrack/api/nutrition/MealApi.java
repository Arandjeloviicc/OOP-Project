package com.fittrack.api.nutrition;

import com.fittrack.api.JsonMapper;
import com.fittrack.dto.nutrition.MealResponse;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

public class MealApi {

    private static final String BASE_URL = "http://localhost:8080/api/nutrition";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = JsonMapper.getMapper();

    public List<MealResponse> getMealsFromDate(Integer userId, LocalDate mealDate) {
        try {
            long start = System.currentTimeMillis();

            String url = BASE_URL + "/user/" + userId + "?date=" + mealDate;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            long end = System.currentTimeMillis();

            System.out.println("Meal API request: " + (end - start) + " ms");

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
}