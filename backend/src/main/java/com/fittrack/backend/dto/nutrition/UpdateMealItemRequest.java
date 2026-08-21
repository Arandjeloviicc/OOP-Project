package com.fittrack.backend.dto.nutrition;

public record UpdateMealItemRequest(
        double quantityGrams,
        String mealType
) {}
