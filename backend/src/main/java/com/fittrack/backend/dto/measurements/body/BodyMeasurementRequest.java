package com.fittrack.backend.dto.measurements.body;

import jakarta.validation.constraints.Positive;

public record BodyMeasurementRequest(
        @Positive double neck,
        @Positive double waist,
        @Positive Double hip
) {}