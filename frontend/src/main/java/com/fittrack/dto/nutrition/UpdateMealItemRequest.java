package com.fittrack.dto.nutrition;

public record UpdateMealItemRequest(
        double quantityGrams,
        String mealType
) {}
