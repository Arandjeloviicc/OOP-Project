package com.fittrack.dto.profile;

import java.time.LocalDate;

public record ProfileResponse(
        String username,
        String email,

        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        double height,

        Double currentWeight,
        Double startWeight,

        String goalType,
        Double goalWeight,
        Double weeklyGoal,
        String activityLevel,

        int targetCalories,
        double targetCarbs,
        double targetFat,
        double targetProtein
) {}
