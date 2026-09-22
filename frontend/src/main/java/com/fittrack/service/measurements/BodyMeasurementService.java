package com.fittrack.service.measurements;

import com.fittrack.api.measurements.BodyMeasurementApi;
import com.fittrack.dto.measurements.body.BodyMeasurementRequest;
import com.fittrack.dto.measurements.body.BodyMeasurementResponse;
import com.fittrack.session.UserSession;

import java.util.List;

public class BodyMeasurementService {

    private final BodyMeasurementApi bodyMeasurementApi;

    public BodyMeasurementService() {
        this.bodyMeasurementApi = new BodyMeasurementApi();
    }

    public List<BodyMeasurementResponse> getBodyMeasurementHistory() {
        return bodyMeasurementApi.getBodyMeasurementHistory(currentUserId());
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