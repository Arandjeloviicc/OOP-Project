package com.fittrack.api.nutrition;

import com.fittrack.api.common.BaseApi;
import com.fittrack.dto.nutrition.meal.*;
import com.fittrack.dto.nutrition.meal.item.*;
import tools.jackson.core.type.TypeReference;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

public class MealApi extends BaseApi {

    private static final String API_URL = NUTRITION_URL + "/meals";

    public List<MealResponse> getMealsForDate(Integer userId, LocalDate mealDate) {
        String url = API_URL + "/user/" + userId + "?date=" + mealDate;

        return apiClient.get(
                url,
                200,
                new TypeReference<>() {}
        );
    }

    public void addMealItem(Integer userId, AddMealItemRequest request) {
        String url = API_URL + "/user/" + userId + "/items";

        apiClient.post(
                url,
                request,
                201
        );
    }

    public MealItemResponse updateMealItem(Integer userId, Integer mealItemId, UpdateMealItemRequest request) {
        String url = API_URL + "/user/" + userId + "/items/" + mealItemId;

        return apiClient.put(
                url,
                request,
                200,
                MealItemResponse.class
        );
    }

    public void deleteMealItem(Integer userId, Integer mealItemId) {
        String url = API_URL + "/user/" + userId + "/items/" + mealItemId;

        apiClient.delete(
                url,
                204
        );
    }

    public List<MealResponse> getMyMeals(Integer userId, String search) {
        String encodedSearch = URLEncoder.encode(
                search == null ? "" : search,
                StandardCharsets.UTF_8
        );

        String url = API_URL + "/mine/" + userId + "?search=" + encodedSearch;

        return apiClient.get(
                url,
                200,
                new TypeReference<>() {}
        );
    }

    public void createSavedMeal(Integer userId, CreateMealRequest request) {
        String url = API_URL + "/mine/" + userId;

        apiClient.post(
                url,
                request,
                201
        );
    }

    public void updateSavedMeal(Integer userId, Integer mealId, UpdateSavedMealRequest request) {
        String url = API_URL + "/mine/" + userId + "/" + mealId;

        apiClient.put(
                url,
                request,
                200
        );
    }

    public void deleteSavedMeal(Integer userId, Integer mealId) {
        String url = API_URL + "/mine/" + userId + "/" + mealId;

        apiClient.delete(
                url,
                204
        );
    }

    public void logSavedMeal(Integer userId, Integer mealId, LogSavedMealRequest request) {
        String url = API_URL +  "/mine/" + userId + "/" + mealId + "/log";

        apiClient.post(
                url,
                request,
                204
        );
    }

    public void copyDailyMeal(Integer userId, CopyMealRequest request) {
        String url = API_URL +  "/user/" + userId + "/copy";

        apiClient.post(
                url,
                request,
                200
        );
    }

    public boolean hasDailyMealItems(Integer userId, LocalDate date, String mealName) {
        String encodedMealName = URLEncoder.encode(
                mealName,
                StandardCharsets.UTF_8
        );

        String url = API_URL + "/user/" + userId + "/has-items" + "?date=" + date + "&mealName=" + encodedMealName;

        return apiClient.get(
                url,
                200,
                Boolean.class
        );
    }
}