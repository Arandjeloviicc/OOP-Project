package com.fittrack.backend.dto.nutrition.meal.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record UpdateMealItemRequest(
        @Positive
        double quantityGrams,

        @NotBlank
        String mealType
) {}
