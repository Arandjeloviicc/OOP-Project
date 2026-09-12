package com.fittrack.backend.dto.nutrition.meal.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateMealItemRequest(
        @Positive double quantityGrams,
        @NotBlank @Size(max = 255) String mealType
) {}
