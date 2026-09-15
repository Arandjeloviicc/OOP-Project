package com.fittrack.api.nutrition;

import com.fittrack.api.common.BaseApi;
import com.fittrack.dto.nutrition.goal.NutritionTargetsResponse;

import java.time.LocalDate;

public class NutritionGoalApi extends BaseApi {

    private static final String API_URL = NUTRITION_URL + "/goals";

    public NutritionTargetsResponse getTargetsForDate(Integer userId, LocalDate date) {
        String url = API_URL + "/user/" + userId + "/targets?date=" + date;

        return apiClient.get(
                url,
                200,
                NutritionTargetsResponse.class
        );
    }
}
