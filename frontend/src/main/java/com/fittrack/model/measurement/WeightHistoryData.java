package com.fittrack.model.measurement;

import com.fittrack.dto.measurements.weight.WeightLogResponse;
import com.fittrack.model.profile.WeightGoal;

import java.util.List;

public record WeightHistoryData(
        List<WeightLogResponse> logs,
        WeightGoal goalType
) {}