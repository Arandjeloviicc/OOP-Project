package com.fittrack.dto.nutrition.meal;

import java.time.LocalDate;

public record LogMealRequest(
        LocalDate mealDate,
        String mealName
) {}
