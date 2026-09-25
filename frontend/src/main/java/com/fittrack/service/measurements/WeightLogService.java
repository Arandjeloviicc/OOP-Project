package com.fittrack.service.measurements;

import com.fittrack.api.measurements.WeightLogApi;
import com.fittrack.dto.measurements.weight.WeightHistoryResponse;
import com.fittrack.dto.measurements.weight.WeightLogRequest;
import com.fittrack.dto.measurements.weight.WeightLogResponse;
import com.fittrack.model.measurements.WeightHistoryData;
import com.fittrack.model.profile.WeightGoal;
import com.fittrack.session.UserSession;

public class WeightLogService {

    private final WeightLogApi weightLogApi;

    public WeightLogService() {
        this.weightLogApi = new WeightLogApi();
    }

    public WeightHistoryData getWeightHistory() {
        WeightHistoryResponse response = weightLogApi.getWeightHistory(currentUserId());

        return new WeightHistoryData(
                response.logs(),
                WeightGoal.valueOf(response.goalType())
        );
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