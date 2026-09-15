package com.fittrack.dto.nutrition.goal;

public record NutritionTargetsResponse(
        int calories,
        double carbs,
        double fat,
        double protein
) {}
