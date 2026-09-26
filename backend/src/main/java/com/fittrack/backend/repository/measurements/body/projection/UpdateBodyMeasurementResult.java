package com.fittrack.backend.repository.measurements.body.projection;

import com.fittrack.backend.dto.measurements.body.BodyMeasurementResponse;

public record UpdateBodyMeasurementResult(
        UpdateBodyMeasurementStatus status,
        BodyMeasurementResponse measurement,
        boolean wasLatest,
        boolean isLatest
) {}