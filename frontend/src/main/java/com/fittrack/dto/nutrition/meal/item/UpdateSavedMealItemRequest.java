package com.fittrack.dto.nutrition.meal.item;

public record UpdateSavedMealItemRequest(
        Integer mealItemId,
        Integer foodId,
        double quantityGrams
) {}
