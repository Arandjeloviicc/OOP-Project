package com.fittrack.service.measurements;

import com.fittrack.api.measurements.WeightLogApi;
import com.fittrack.dto.measurements.weight.WeightLogRequest;
import com.fittrack.dto.measurements.weight.WeightLogResponse;
import com.fittrack.session.UserSession;

import java.util.List;

public class WeightLogService {

    private final WeightLogApi weightLogApi;

    public WeightLogService() {
        this.weightLogApi = new WeightLogApi();
    }

    public List<WeightLogResponse> getWeightHistory() {
        return weightLogApi.getWeightHistory(currentUserId());
    }

    public WeightLogResponse createWeightLog(WeightLogRequest request) {
        return weightLogApi.createWeightLog(currentUserId(), request);
    }

    public WeightLogResponse updateWeightLog(Integer weightLogId, WeightLogRequest request) {
        return weightLogApi.updateWeightLog(currentUserId(), weightLogId, request);
    }

    public void deleteWeightLog(Integer weightLogId) {
        weightLogApi.deleteWeightLog(currentUserId(), weightLogId);
    }

    // ── Helpers ─────────────────────────────────────────────
    private Integer currentUserId() {
        return UserSession.getInstance().requireCurrentUser().id();
    }
}