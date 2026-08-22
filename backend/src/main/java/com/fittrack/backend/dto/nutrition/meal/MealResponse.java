package com.fittrack.backend.dto.nutrition.meal;

import com.fittrack.backend.dto.nutrition.meal.item.MealItemResponse;

import java.time.LocalDate;
import java.util.List;

public record MealResponse(
        Integer id,
        String name,
        LocalDate mealDate,
        List<MealItemResponse> items
) {
}
