package com.fittrack.dto.nutrition.meal.item;

public record CreateMealItemRequest(
        Integer foodId,
        String foodName,
        String brand,
        double quantityGrams,
        double servingSizeGrams,
        double caloriesPerServing,
        double proteinPerServing,
        double carbsPerServing,
        double fatPerServing
) {}
