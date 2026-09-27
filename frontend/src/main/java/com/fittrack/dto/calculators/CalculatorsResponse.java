package com.fittrack.dto.calculators;

import java.time.LocalDate;

public record CalculatorsResponse(
        double height,
        double weight,
        LocalDate dateOfBirth,
        String gender,
        String activityLevel,
        Double neck,
        Double waist,
        Double hip
) {}