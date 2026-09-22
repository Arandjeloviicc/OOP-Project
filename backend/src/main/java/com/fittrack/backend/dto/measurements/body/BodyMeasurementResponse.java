package com.fittrack.backend.dto.measurements.body;

import java.time.Instant;

public record BodyMeasurementResponse(
        Integer id,
        double neck,
        double waist,
        Double hip,
        Instant loggedAt
) {}