package com.fittrack.backend.dto.measurements.weight;

import com.fittrack.backend.entity.profile.WeightGoal;

import java.util.List;

public record WeightHistoryResponse(
        List<WeightLogResponse> logs,
        WeightGoal goalType
) {}