package com.fittrack.backend.dto.measurements.body;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record BodyMeasurementRequest(
        @Positive double neck,
        @Positive double waist,
        @Positive Double hip,
        @NotNull Instant loggedAt
) {}