package com.fittrack.dto.nutrition.meal;

import com.fittrack.dto.nutrition.meal.item.UpdateSavedMealItemRequest;

import java.util.List;

public record UpdateSavedMealRequest(
        String name,
        List<UpdateSavedMealItemRequest> items
) {}
