package com.fittrack.backend.repository.measurements.body.projection;

public record DeleteBodyMeasurementResult(
        boolean deleted,
        boolean wasLatest
) {}