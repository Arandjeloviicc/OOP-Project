package com.fittrack.backend.dto.profile.editor;

import com.fittrack.backend.entity.profile.ActivityLevel;
import com.fittrack.backend.entity.profile.WeightGoal;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record NutritionGoalUpdateRequest(
        @NotNull
        ActivityLevel activityLevel,

        @NotNull
        WeightGoal goalType,

        @DecimalMin("30.0")
        @DecimalMax("300.0")
        Double goalWeight,

        @DecimalMin("0.25")
        @DecimalMax("1.0")
        Double weeklyGoal
) {}