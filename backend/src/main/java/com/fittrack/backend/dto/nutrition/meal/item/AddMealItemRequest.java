package com.fittrack.backend.dto.nutrition.meal.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AddMealItemRequest(
        @NotNull LocalDate mealDate,
        @NotBlank @Size(max = 255) String mealName,
        @NotNull Integer foodId,
        @Positive double quantityGrams
) {}