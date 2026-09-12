package com.fittrack.backend.dto.nutrition.food;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateFoodRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 255) String brand,
        @Positive double servingSizeGrams,
        @PositiveOrZero double caloriesPerServing,
        @PositiveOrZero double proteinPerServing,
        @PositiveOrZero double carbsPerServing,
        @PositiveOrZero double fatPerServing
) {}