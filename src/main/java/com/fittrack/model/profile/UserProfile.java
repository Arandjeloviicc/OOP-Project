package com.fittrack.model.profile;

import java.time.LocalDate;

public record UserProfile(
        int userId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Gender gender,
        double height,
        ActivityLevel activityLevel,
        WeightGoal goalType,
        Double goalWeight,
        Double weeklyGoal
) {
}
