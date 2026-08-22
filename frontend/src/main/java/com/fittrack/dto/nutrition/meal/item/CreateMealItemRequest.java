package com.fittrack.dto.nutrition.meal.item;

public record CreateMealItemRequest(
        Integer foodId,
        double quantityGrams
) {}
