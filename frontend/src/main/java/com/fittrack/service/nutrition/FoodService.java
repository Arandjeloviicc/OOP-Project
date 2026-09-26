package com.fittrack.service.nutrition;

import com.fittrack.api.nutrition.FoodApi;
import com.fittrack.dto.nutrition.food.FoodRequest;
import com.fittrack.dto.nutrition.food.FoodResponse;
import com.fittrack.session.UserSession;

import java.util.List;

public class FoodService {

    private final FoodApi foodApi;

    public FoodService() {
        this.foodApi = new FoodApi();
    }

    public List<FoodResponse> searchAllFoods(String search) {
        return foodApi.searchFoods(search);
    }

    public List<FoodResponse> searchMyFoods(String search) {
        return foodApi.getMyFoods(
                currentUserId(),
                search
        );
    }

    public FoodResponse createFood(FoodRequest request) {
        return foodApi.createFood(
                currentUserId(),
                request
        );
    }

    public FoodResponse updateFood(Integer foodId, FoodRequest request) {
        return foodApi.updateFood(
                currentUserId(),
                foodId,
                request
        );
    }

    public void deleteFood(Integer foodId) {
        foodApi.deleteFood(
                currentUserId(),
                foodId
        );
    }

    // ── Helpers ─────────────────────────────────────────────
    private Integer currentUserId() {
        return UserSession.getInstance().requireCurrentUser().id();
    }
}