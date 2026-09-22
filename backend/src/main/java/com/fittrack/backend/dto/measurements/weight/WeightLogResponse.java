package com.fittrack.backend.dto.measurements.weight;

import java.time.Instant;

public record WeightLogResponse(
        Integer id,
        double weight,
        Instant loggedAt
) {}