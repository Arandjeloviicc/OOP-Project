package com.fittrack.backend.dto.measurements.weight;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record WeightLogRequest(
        @DecimalMin("30.0")
        @DecimalMax("300.0")
        double weight,

        @NotNull
        Instant loggedAt
) {}