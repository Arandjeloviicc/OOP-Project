package com.fittrack.backend.dto.nutrition.food;

public record FoodResponse(
        Integer id,
        String name,
        String brand,
        double servingSizeGrams,
        double caloriesPerServing,
        double proteinPerServing,
        double carbsPerServing,
        double fatPerServing,
        Integer createdByUserId
) {
}
