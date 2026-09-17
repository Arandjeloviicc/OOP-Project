package com.fittrack.service.profile;

import com.fittrack.api.profile.ProfileApi;
import com.fittrack.dto.profile.editor.PersonalInfoUpdateRequest;
import com.fittrack.model.profile.ProfileData;
import com.fittrack.dto.profile.ProfileResponse;
import com.fittrack.model.profile.ActivityLevel;
import com.fittrack.model.profile.Gender;
import com.fittrack.model.profile.WeightGoal;
import com.fittrack.session.UserSession;

public class ProfileService {

    private final ProfileApi profileApi;
    private final UserSession userSession;

    public ProfileService() {
        this.profileApi = new ProfileApi();
        this.userSession = UserSession.getInstance();
    }

    public ProfileData getProfile() {
        ProfileResponse response = profileApi.getProfile(currentUserId());

        return new ProfileData(
                response.username(),
                response.email(),

                response.firstName(),
                response.lastName(),
                response.dateOfBirth(),
                Gender.valueOf(response.gender()),
                response.height(),

                response.currentWeight(),
                response.startWeight(),

                WeightGoal.valueOf(response.goalType()),
                response.goalWeight(),
                response.weeklyGoal(),
                ActivityLevel.valueOf(response.activityLevel()),

                response.targetCalories(),
                response.targetCarbs(),
                response.targetFat(),
                response.targetProtein()
        );
    }

    public void updatePersonalInfo(PersonalInfoUpdateRequest request) {
        profileApi.updatePersonalInfo(currentUserId(), request);
    }

    // ── Helpers ─────────────────────────────────────────────────
    private Integer currentUserId() {
        return userSession.requireCurrentUser().id();
    }
}