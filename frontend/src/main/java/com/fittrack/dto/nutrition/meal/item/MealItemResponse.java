package com.fittrack.dto.nutrition.meal.item;

public record MealItemResponse(
        Integer id,
        Integer foodId,
        String foodName,
        String brand,
        double quantityGrams,
        double servingSizeGrams,
        double caloriesPerServing,
        double proteinPerServing,
        double carbsPerServing,
        double fatPerServing
) {
}