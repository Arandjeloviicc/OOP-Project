package com.fittrack.backend.repository.measurements.weight.projection;

public record DeleteWeightLogResult(
        DeleteWeightLogStatus status,
        boolean wasLatest
) {}