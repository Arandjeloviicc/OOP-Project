package com.fittrack.api.profile;

import com.fittrack.api.common.BaseApi;
import com.fittrack.dto.nutrition.goal.NutritionTargetsResponse;

public class ProfileApi extends BaseApi {

    public NutritionTargetsResponse getNutritionTargets(Integer userId) {
        // Trenutno hardcoded dok ne implementiram ucitavanje podataka
        return new NutritionTargetsResponse(
                2200,
                250,
                70,
                160
        );
    }
}
