package com.fittrack.dto.profile.editor;

public record NutritionGoalUpdateRequest(
        String activityLevel,
        String goalType,
        Double goalWeight,
        Double weeklyGoal
) {}
