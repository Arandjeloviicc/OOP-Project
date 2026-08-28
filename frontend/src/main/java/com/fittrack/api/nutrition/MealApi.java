package com.fittrack.api.nutrition;

import com.fittrack.api.common.BaseApi;
import com.fittrack.dto.nutrition.meal.CreateMealRequest;
import com.fittrack.dto.nutrition.meal.LogMealRequest;
import com.fittrack.dto.nutrition.meal.UpdateSavedMealRequest;
import com.fittrack.dto.nutrition.meal.item.*;
import com.fittrack.dto.nutrition.meal.MealResponse;
import tools.jackson.core.type.TypeReference;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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
                    "Meal get request was interrupted.",
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
                    "Meal item add request was interrupted.",
                    exception
            );
        }
    }

    public MealItemResponse updateMealItem(Integer userId, Integer mealItemId, UpdateMealItemRequest requestData) {
        try {
            String url = API_URL + "/user/" + userId + "/items/" + mealItemId;

            String requestBody = objectMapper.writeValueAsString(requestData);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Failed to update meal item."
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
                    "Meal item update request was interrupted.",
                    exception
            );
        }
    }

    public void deleteMealItem(Integer userId, Integer mealItemId) {
        try {
            String url = API_URL + "/user/" + userId + "/items/" + mealItemId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 204) {
                throw new IllegalStateException(
                        "Failed to delete meal item. Status: "
                                + response.statusCode()
                                + ", body: "
                                + response.body()
                );
            }

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not communicate with the FitTrack server.",
                    exception
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Meal item delete request was interrupted.",
                    exception
            );
        }
    }

    public List<MealResponse> getMyMeals(Integer userId, String search) {
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
                        "Failed to load user meals."
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

    public MealResponse createMyMeal(Integer userId, CreateMealRequest requestData) {
        try {
            String url = API_URL + "/mine/" + userId;

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
                throw new IOException(
                        "Failed to create meal. Status: "
                                + response.statusCode()
                );
            }

            return objectMapper.readValue(
                    response.body(),
                    MealResponse.class
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not communicate with the FitTrack server.",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Meal creation request was interrupted.",
                    exception
            );
        }
    }

    public MealResponse updateMyMeal(Integer userId, Integer mealId, UpdateSavedMealRequest requestData) {
        try {
            String url = API_URL + "/mine/" + userId + "/" + mealId;

            String requestBody = objectMapper.writeValueAsString(requestData);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new IOException(
                        "Failed to update meal. Status: " + response.statusCode()
                );
            }

            return objectMapper.readValue(
                    response.body(),
                    MealResponse.class
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not communicate with the FitTrack server.",
                    exception
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "My Meal update request was interrupted.",
                    exception
            );
        }
    }

    public void deleteMyMeal(Integer userId, Integer mealId) {
        try {
            String url = API_URL + "/mine/" + userId + "/" + mealId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 204) {
                throw new IOException(
                        "Failed to delete meal. Status: " + response.statusCode()
                );
            }

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not communicate with the FitTrack server.",
                    exception
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "My Meal delete request was interrupted.",
                    exception
            );
        }
    }

    public void logMyMeal(Integer userId, Integer mealId, LogMealRequest requestData) {
        try {

            String url = API_URL +  "/mine/" + userId + "/" + mealId + "/log";

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

            if (response.statusCode() != 204) {
                throw new IOException(
                        "Failed to log meal. Status: " + response.statusCode()
                );
            }

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not communicate with the FitTrack server.",
                    exception
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "My Meal log request was interrupted.",
                    exception
            );
        }
    }
}