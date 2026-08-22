package com.fittrack.backend.dto.nutrition.meal.item;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateMealItemRequest(
        @NotNull @Positive Integer foodId,
        @Positive double quantityGrams
) {}
