package com.fittrack.backend.dto.nutrition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record AddMealItemRequest(
        @NotNull LocalDate mealDate,
        @NotBlank String mealName,
        @NotNull Integer foodId,
        @Positive double quantityGrams
) {}