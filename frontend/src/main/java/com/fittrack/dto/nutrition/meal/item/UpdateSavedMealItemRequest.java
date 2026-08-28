package com.fittrack.dto.nutrition.meal.item;

public record UpdateSavedMealItemRequest(
        Integer mealItemId,
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