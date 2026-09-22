package com.fittrack.dto.measurements.body;

public record BodyMeasurementRequest(
        double neck,
        double waist,
        Double hip
) {}