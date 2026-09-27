package com.fittrack.backend.dto.measurements.weight;

import com.fittrack.backend.domain.profile.WeightGoal;

import java.util.List;

public record WeightHistoryResponse(
        List<WeightLogResponse> logs,
        WeightGoal goalType
) {}