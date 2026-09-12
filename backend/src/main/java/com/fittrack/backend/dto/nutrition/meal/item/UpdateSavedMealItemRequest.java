package com.fittrack.backend.dto.nutrition.meal.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateSavedMealItemRequest(
        @Positive Integer mealItemId,
        @Positive Integer foodId,
        @NotBlank @Size(max = 255) String foodName,
        @Size(max = 255) String brand,
        @Positive double quantityGrams,
        @Positive double servingSizeGrams,
        @PositiveOrZero double caloriesPerServing,
        @PositiveOrZero double proteinPerServing,
        @PositiveOrZero double carbsPerServing,
        @PositiveOrZero double fatPerServing
) {}