package com.fittrack.dto.nutrition.meal;

import com.fittrack.dto.nutrition.meal.item.CreateMealItemRequest;

import java.util.List;

public record CreateMealRequest(
        String name,
        List<CreateMealItemRequest> items
) {}
