package com.fittrack.backend.repository.nutrition.goal;

import com.fittrack.backend.entity.profile.ActivityLevel;
import com.fittrack.backend.entity.profile.Gender;
import com.fittrack.backend.entity.profile.WeightGoal;

import java.time.LocalDate;

public record NutritionGoalRecalculationData(
        Integer goalId,

        LocalDate dateOfBirth,
        Gender gender,
        double height,
        Double currentWeight,

        ActivityLevel activityLevel,
        WeightGoal goalType,
        Double goalWeight,
        Double weeklyGoal,

        LocalDate startDate
) {}