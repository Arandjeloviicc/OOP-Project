package com.fittrack.backend.repository.nutrition.meal.item.projection;

public record CreateSavedMealResult(
        boolean userExists,
        int requestedCount,
        int validCount,
        int insertedCount
) {}
