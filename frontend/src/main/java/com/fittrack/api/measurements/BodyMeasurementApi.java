package com.fittrack.api.measurements;

import com.fittrack.api.common.BaseApi;
import com.fittrack.dto.measurements.body.BodyMeasurementRequest;
import com.fittrack.dto.measurements.body.BodyMeasurementResponse;
import tools.jackson.core.type.TypeReference;

import java.util.List;

public class BodyMeasurementApi extends BaseApi {

    private static final String API_URL = MEASUREMENTS_URL + "/body";

    public List<BodyMeasurementResponse> getBodyMeasurementHistory(Integer userId) {
        String url = API_URL + "/user/" + userId;

        return apiClient.get(
                url,
                200,
                new TypeReference<>() {}
        );
    }

    public BodyMeasurementResponse createBodyMeasurement(Integer userId, BodyMeasurementRequest request) {
        String url = API_URL + "/user/" + userId;

        return apiClient.post(
                url,
                request,
                201,
                BodyMeasurementResponse.class
        );
    }

    public BodyMeasurementResponse updateBodyMeasurement(Integer userId, Integer bodyMeasurementId, BodyMeasurementRequest request) {
        String url = API_URL + "/user/" + userId + "/" + bodyMeasurementId;

        return apiClient.put(
                url,
                request,
                200,
                BodyMeasurementResponse.class
        );
    }

    public void deleteBodyMeasurement(Integer userId, Integer bodyMeasurementId) {
        String url = API_URL + "/user/" + userId + "/" + bodyMeasurementId;

        apiClient.delete(
                url,
                204
        );
    }
}