package com.fittrack.api.measurements;

import com.fittrack.api.common.BaseApi;
import com.fittrack.dto.measurements.weight.WeightHistoryResponse;
import com.fittrack.dto.measurements.weight.WeightLogRequest;
import com.fittrack.dto.measurements.weight.WeightLogResponse;
import tools.jackson.core.type.TypeReference;

public class WeightLogApi extends BaseApi {

    private static final String API_URL = MEASUREMENTS_URL + "/weights";

    public WeightHistoryResponse getWeightHistory(Integer userId) {
        String url = API_URL + "/user/" + userId;

        return apiClient.get(
                url,
                200,
                new TypeReference<>() {}
        );
    }

    public WeightLogResponse createWeightLog(Integer userId, WeightLogRequest request) {
        String url = API_URL + "/user/" + userId;

        return apiClient.post(
                url,
                request,
                201,
                WeightLogResponse.class
        );
    }

    public WeightLogResponse updateWeightLog(Integer userId, Integer weightLogId, WeightLogRequest request) {
        String url = API_URL + "/user/" + userId + "/" + weightLogId;

        return apiClient.put(
                url,
                request,
                200,
                WeightLogResponse.class
        );
    }

    public void deleteWeightLog(Integer userId, Integer weightLogId) {
        String url = API_URL + "/user/" + userId + "/" + weightLogId;

        apiClient.delete(
                url,
                204
        );
    }
}