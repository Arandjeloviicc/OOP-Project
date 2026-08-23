package com.fittrack.dto.nutrition.meal.item;

import java.util.List;

public record UpdateSavedMealRequest(
        String name,
        List<UpdateSavedMealItemRequest> items
) {}
