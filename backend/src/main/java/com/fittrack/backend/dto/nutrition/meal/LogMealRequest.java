package com.fittrack.backend.dto.nutrition.meal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LogMealRequest(
        @NotNull LocalDate mealDate,
        @NotBlank String mealName
) {
}
