package com.fittrack.service.profile;

import com.fittrack.api.profile.ProfileSetupApi;
import com.fittrack.model.profile.ProfileSetupData;
import com.fittrack.session.UserSession;

public class ProfileSetupService {

    private final ProfileSetupApi profileSetupApi;
    private final UserSession userSession;

    public ProfileSetupService() {
        this.profileSetupApi = new ProfileSetupApi();
        this.userSession = UserSession.getInstance();
    }

    public void completeSetup(ProfileSetupData data) {
        com.fittrack.dto.profile.ProfileSetupRequest request = new com.fittrack.dto.profile.ProfileSetupRequest(
                currentUserId(),
                data.firstName(),
                data.lastName(),
                data.dateOfBirth(),
                data.gender().name(),
                data.height(),
                data.activityLevel().name(),
                data.goalType().name(),
                data.goalWeight(),
                data.weeklyGoal(),
                data.weight()
        );

        profileSetupApi.completeProfile(request);
    }

    // ── Helpers ─────────────────────────────────────────────
    private Integer currentUserId() {
        return userSession.requireCurrentUser().id();
    }
}
