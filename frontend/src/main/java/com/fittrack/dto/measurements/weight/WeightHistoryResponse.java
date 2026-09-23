package com.fittrack.dto.measurements.weight;

import java.util.List;

public record WeightHistoryResponse(
        List<WeightLogResponse> logs,
        String goalType
) {}