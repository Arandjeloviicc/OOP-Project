package com.fittrack.dto.nutrition.food;

public record CreateFoodRequest(
        String name,
        String brand,
        double servingSizeGrams,
        double caloriesPerServing,
        double proteinPerServing,
        double carbsPerServing,
        double fatPerServing
) {}
