package com.fittrack.service.measurements;

import com.fittrack.api.measurements.BodyMeasurementApi;
import com.fittrack.dto.measurements.body.BodyMeasurementHistoryResponse;
import com.fittrack.dto.measurements.body.BodyMeasurementRequest;
import com.fittrack.dto.measurements.body.BodyMeasurementResponse;
import com.fittrack.model.measurements.BodyMeasurementHistoryData;
import com.fittrack.model.profile.Gender;
import com.fittrack.session.UserSession;

public class BodyMeasurementService {

    private final BodyMeasurementApi bodyMeasurementApi;

    public BodyMeasurementService() {
        this.bodyMeasurementApi = new BodyMeasurementApi();
    }

    public BodyMeasurementHistoryData getBodyMeasurementHistory() {
        BodyMeasurementHistoryResponse response = bodyMeasurementApi.getBodyMeasurementHistory(currentUserId());

        return new BodyMeasurementHistoryData(
                response.measurements(),
                Gender.valueOf(response.gender())
        );
    }

    public BodyMeasurementResponse createBodyMeasurement(BodyMeasurementRequest request) {
        return bodyMeasurementApi.createBodyMeasurement(currentUserId(), request);
    }

    public BodyMeasurementResponse updateBodyMeasurement(Integer bodyMeasurementId, BodyMeasurementRequest request) {
        return bodyMeasurementApi.updateBodyMeasurement(currentUserId(), bodyMeasurementId, request);
    }

    public void deleteBodyMeasurement(Integer bodyMeasurementId) {
        bodyMeasurementApi.deleteBodyMeasurement(currentUserId(), bodyMeasurementId);
    }

    // ── Helpers ─────────────────────────────────────────────
    private Integer currentUserId() {
        return UserSession.getInstance().requireCurrentUser().id();
    }
}