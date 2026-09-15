package com.fittrack.model.profile;

import java.time.LocalDate;

public record ProfileData(
        String username,
        String email,

        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Gender gender,
        double height,

        Double currentWeight,
        Double startWeight,

        WeightGoal goalType,
        Double goalWeight,
        Double weeklyGoal,
        ActivityLevel activityLevel,

        int targetCalories,
        double targetCarbs,
        double targetFat,
        double targetProtein
) {}