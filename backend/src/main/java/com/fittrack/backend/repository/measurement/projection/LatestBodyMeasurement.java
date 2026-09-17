package com.fittrack.backend.repository.measurement.projection;

public record LatestBodyMeasurement(
        Double neck,
        Double waist,
        Double hip
) {}