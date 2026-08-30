package com.fittrack.dto.nutrition.meal;

import java.time.LocalDate;

public record CopyMealRequest(
        LocalDate sourceDate,
        String sourceMealName,
        LocalDate targetDate,
        String targetMealName
) {}
