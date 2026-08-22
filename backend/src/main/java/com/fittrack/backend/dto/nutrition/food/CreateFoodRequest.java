package com.fittrack.backend.dto.nutrition.food;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateFoodRequest(
        @NotBlank String name,
        String brand,
        @Positive double servingSizeGrams,
        @PositiveOrZero double caloriesPerServing,
        @PositiveOrZero double proteinPerServing,
        @PositiveOrZero double carbsPerServing,
        @PositiveOrZero double fatPerServing
) {}