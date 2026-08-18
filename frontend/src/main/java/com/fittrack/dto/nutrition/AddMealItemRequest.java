package com.fittrack.dto.nutrition;

import java.time.LocalDate;

public record AddMealItemRequest(
        LocalDate mealDate,
        String mealName,
        Integer foodId,
        double quantityGrams
) {}
