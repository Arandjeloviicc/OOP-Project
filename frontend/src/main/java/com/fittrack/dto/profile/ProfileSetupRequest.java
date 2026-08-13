package com.fittrack.dto.profile;

import java.time.LocalDate;

public record ProfileSetupRequest(
        int userId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        double height,
        String activityLevel,
        String goalType,
        Double goalWeight,
        Double weeklyGoal,
        double weight
) {
}