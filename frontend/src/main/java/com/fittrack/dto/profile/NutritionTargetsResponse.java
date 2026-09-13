package com.fittrack.dto.profile;

public record NutritionTargetsResponse(
        int calories,
        double carbs,
        double fat,
        double protein
) {}
