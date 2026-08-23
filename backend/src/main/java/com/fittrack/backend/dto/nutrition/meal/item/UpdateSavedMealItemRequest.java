package com.fittrack.backend.dto.nutrition.meal.item;

import jakarta.validation.constraints.Positive;

public record UpdateSavedMealItemRequest(
        Integer mealItemId,
        Integer foodId,
        @Positive double quantityGrams
) {}
