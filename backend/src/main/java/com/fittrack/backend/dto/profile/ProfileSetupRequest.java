package com.fittrack.backend.dto.profile;

import com.fittrack.backend.entity.profile.ActivityLevel;
import com.fittrack.backend.entity.profile.Gender;
import com.fittrack.backend.entity.profile.WeightGoal;

import java.time.LocalDate;

public record ProfileSetupRequest(
        Integer userId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Gender gender,
        double height,
        ActivityLevel activityLevel,
        WeightGoal goalType,
        Double goalWeight,
        Double weeklyGoal,
        double weight
) {
}
