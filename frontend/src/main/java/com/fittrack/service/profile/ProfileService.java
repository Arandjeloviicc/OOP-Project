package com.fittrack.service.profile;

import com.fittrack.api.profile.ProfileApi;
import com.fittrack.dto.profile.NutritionTargetsResponse;
import com.fittrack.model.nutrition.NutritionTargets;
import com.fittrack.session.UserSession;

public class ProfileService {

    private final ProfileApi profileApi;
    private final UserSession userSession;

    public ProfileService() {
        this.profileApi = new ProfileApi();
        this.userSession = UserSession.getInstance();
    }

    public NutritionTargets getNutritionTargets() {
        NutritionTargetsResponse response = profileApi.getNutritionTargets(currentUserId());

        return new NutritionTargets(
                response.calories(),
                response.carbs(),
                response.fat(),
                response.protein()
        );
    }

    // ── Helpers ─────────────────────────────────────────────
    private Integer currentUserId() {
        return userSession.requireCurrentUser().id();
    }
}