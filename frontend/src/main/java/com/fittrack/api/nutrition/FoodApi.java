package com.fittrack.api.nutrition;

import com.fittrack.api.common.BaseApi;
import com.fittrack.dto.nutrition.food.FoodRequest;
import com.fittrack.dto.nutrition.food.FoodResponse;
import tools.jackson.core.type.TypeReference;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FoodApi extends BaseApi {

    private static final String API_URL = NUTRITION_URL + "/foods";

    public List<FoodResponse> searchFoods(String search) {
        String encodedSearch = URLEncoder.encode(
                search,
                StandardCharsets.UTF_8
        );

        String url = API_URL + "?search=" + encodedSearch;

        return apiClient.get(
                url,
                200,
                new TypeReference<>() {}
        );
    }

    public List<FoodResponse> getMyFoods(Integer userId, String search) {
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

    public FoodResponse createFood(Integer userId, FoodRequest request) {
        String url = API_URL + "/user/" + userId;

        return apiClient.post(
                url,
                request,
                201,
                FoodResponse.class
        );
    }

    public FoodResponse updateFood(Integer userId, Integer foodId, FoodRequest request) {
        String url = API_URL + "/user/" + userId + "/" + foodId;

        return apiClient.put(
                url,
                request,
                200,
                FoodResponse.class
        );
    }

    public void deleteFood(Integer userId, Integer foodId) {
        String url = API_URL + "/user/" + userId + "/" + foodId;

        apiClient.delete(
                url,
                204
        );
    }
}