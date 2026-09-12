package com.fittrack.backend.dto.nutrition.meal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record LogSavedMealRequest(
        @NotNull LocalDate mealDate,
        @NotBlank @Size(max = 255) String mealName
) {
}
