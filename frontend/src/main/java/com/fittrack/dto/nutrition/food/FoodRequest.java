package com.fittrack.dto.nutrition.food;

public record FoodRequest(
        String name,
        String brand,
        double servingSizeGrams,
        double caloriesPerServing,
        double proteinPerServing,
        double carbsPerServing,
        double fatPerServing
) {}
