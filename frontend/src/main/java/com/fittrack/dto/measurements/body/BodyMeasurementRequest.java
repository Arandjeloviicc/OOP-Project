package com.fittrack.dto.measurements.body;

import java.time.Instant;

public record BodyMeasurementRequest(
        double neck,
        double waist,
        Double hip,
        Instant loggedAt
) {}