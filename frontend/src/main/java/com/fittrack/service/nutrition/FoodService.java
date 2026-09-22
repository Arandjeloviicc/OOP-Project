package com.fittrack.service.nutrition;

import com.fittrack.api.nutrition.FoodApi;
import com.fittrack.dto.nutrition.food.CreateFoodRequest;
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

    public FoodResponse createFood(CreateFoodRequest request) {
        return foodApi.createFood(
                currentUserId(),
                request
        );
    }

    // ── Helpers ─────────────────────────────────────────────
    private Integer currentUserId() {
        return UserSession.getInstance().requireCurrentUser().id();
    }
}