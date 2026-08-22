package com.fittrack.dto.nutrition.meal.item;

public record UpdateMealItemRequest(
        double quantityGrams,
        String mealType
) {}
