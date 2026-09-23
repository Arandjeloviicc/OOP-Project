package com.fittrack.backend.repository.measurements.weight.projection;

import com.fittrack.backend.dto.measurements.weight.WeightLogResponse;

public record CreateWeightLogResult(
        WeightLogResponse weightLog,
        boolean isLatest
) {}