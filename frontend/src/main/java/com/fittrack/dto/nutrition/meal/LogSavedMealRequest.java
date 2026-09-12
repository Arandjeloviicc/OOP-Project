package com.fittrack.dto.nutrition.meal;

import java.time.LocalDate;

public record LogSavedMealRequest(
        LocalDate mealDate,
        String mealName
) {}
