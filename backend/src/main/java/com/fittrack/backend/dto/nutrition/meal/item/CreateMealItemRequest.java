package com.fittrack.backend.dto.nutrition.meal.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateMealItemRequest(
        @Positive Integer foodId,
        @NotBlank String foodName,
        String brand,
        @Positive double quantityGrams,
        @Positive double servingSizeGrams,
        @PositiveOrZero double caloriesPerServing,
        @PositiveOrZero double proteinPerServing,
        @PositiveOrZero double carbsPerServing,
        @PositiveOrZero double fatPerServing
) {}
