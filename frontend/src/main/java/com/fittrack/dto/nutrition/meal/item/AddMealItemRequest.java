package com.fittrack.dto.nutrition.meal.item;

import java.time.LocalDate;

public record AddMealItemRequest(
        LocalDate mealDate,
        String mealName,
        Integer foodId,
        double quantityGrams
) {}
