package com.fittrack.api.profile;

import com.fittrack.api.common.BaseApi;
import com.fittrack.dto.profile.ProfileResponse;
import com.fittrack.dto.profile.editor.PersonalInfoUpdateRequest;

public class ProfileApi extends BaseApi {

    private static final String API_URL = PROFILE_URL;

    public ProfileResponse getProfile(Integer userId) {
        String url = API_URL + "/user/" + userId;

        return apiClient.get(
                url,
                200,
                ProfileResponse.class
        );
    }

    public void updatePersonalInfo(Integer userId, PersonalInfoUpdateRequest request) {
        String url = API_URL + "/user/" + userId + "/personal-info";

        apiClient.put(
                url,
                request,
                204
        );
    }
}
