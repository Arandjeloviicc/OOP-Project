package com.fittrack.backend.repository.measurements.body.projection;

import com.fittrack.backend.dto.measurements.body.BodyMeasurementResponse;

public record CreateBodyMeasurementResult(
        CreateBodyMeasurementStatus status,
        BodyMeasurementResponse bodyMeasurement,
        boolean isLatest
) {}