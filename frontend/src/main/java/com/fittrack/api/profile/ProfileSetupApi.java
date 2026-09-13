package com.fittrack.api.profile;

import com.fittrack.api.common.BaseApi;
import com.fittrack.dto.profile.ProfileSetupRequest;

public class ProfileSetupApi extends BaseApi {

    private static final String API_URL = PROFILE_URL + "/setup";

    public void completeProfile(ProfileSetupRequest request) {
        apiClient.post(
                API_URL,
                request,
                200
        );
    }
}
