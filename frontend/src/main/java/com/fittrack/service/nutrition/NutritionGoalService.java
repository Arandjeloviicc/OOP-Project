package com.fittrack.service.nutrition;

import com.fittrack.api.nutrition.NutritionGoalApi;
import com.fittrack.dto.nutrition.goal.NutritionTargetsResponse;
import com.fittrack.model.nutrition.NutritionTargets;
import com.fittrack.session.UserSession;

import java.time.LocalDate;

public class NutritionGoalService {

    private final NutritionGoalApi nutritionGoalApi;

    public NutritionGoalService() {
        this.nutritionGoalApi = new NutritionGoalApi();
    }

    public NutritionTargets getNutritionTargetsForDate(LocalDate date) {
        NutritionTargetsResponse response = nutritionGoalApi.getTargetsForDate(currentUserId(), date);

        return new NutritionTargets(
                response.calories(),
                response.carbs(),
                response.fat(),
                response.protein()
        );
    }

    // ── Helpers ─────────────────────────────────────────────
    private Integer currentUserId() {
        return UserSession.getInstance().requireCurrentUser().id();
    }
}
