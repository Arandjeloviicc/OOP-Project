package com.fittrack.backend.dto.calculators;

import com.fittrack.backend.domain.profile.ActivityLevel;
import com.fittrack.backend.domain.profile.Gender;

import java.time.LocalDate;

public record CalculatorsResponse(
        double height,
        double weight,
        LocalDate dateOfBirth,
        Gender gender,
        ActivityLevel activityLevel,
        Double neck,
        Double waist,
        Double hip
) {}