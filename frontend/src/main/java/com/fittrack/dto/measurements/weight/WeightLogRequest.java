package com.fittrack.dto.measurements.weight;

import java.time.Instant;

public record WeightLogRequest(
        double weight,
        Instant loggedAt
) {}