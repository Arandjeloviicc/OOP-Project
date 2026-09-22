package com.fittrack.backend.repository.measurements.body.projection;

public record LatestBodyMeasurement(
        Double neck,
        Double waist,
        Double hip
) {}