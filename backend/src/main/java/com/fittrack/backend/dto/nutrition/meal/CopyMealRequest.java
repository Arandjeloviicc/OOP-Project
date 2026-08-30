package com.fittrack.backend.dto.nutrition.meal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CopyMealRequest(
        @NotNull LocalDate sourceDate,
        @NotBlank String sourceMealName,
        @NotNull LocalDate targetDate,
        @NotBlank String targetMealName
) {}
