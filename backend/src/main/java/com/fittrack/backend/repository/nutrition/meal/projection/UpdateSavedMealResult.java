package com.fittrack.backend.repository.nutrition.meal.projection;

public record UpdateSavedMealResult(
        boolean mealExists,
        boolean savedMeal,
        boolean belongsToUser,
        int requestedExistingCount,
        int validExistingCount,
        int requestedNewCount,
        int validNewCount,
        int updatedMealCount,
        int insertedCount
) {}