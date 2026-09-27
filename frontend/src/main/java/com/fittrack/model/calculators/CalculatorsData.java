package com.fittrack.model.calculators;

import com.fittrack.model.profile.ActivityLevel;
import com.fittrack.model.profile.Gender;

public record CalculatorsData(
        double height,
        double weight,
        int age,
        Gender gender,
        ActivityLevel activityLevel,
        Double neck,
        Double waist,
        Double hip
) {}