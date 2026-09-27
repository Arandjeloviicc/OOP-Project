package com.fittrack.api.calculators;

import com.fittrack.api.common.BaseApi;
import com.fittrack.dto.calculators.CalculatorsResponse;

public class CalculatorsApi extends BaseApi {

    private static final String API_URL = CALCULATORS_URL;

    public CalculatorsResponse getCalculatorsData(Integer userId) {
        String url = API_URL + "/user/" + userId;

        return apiClient.get(
                url,
                200,
                CalculatorsResponse.class
        );
    }
}