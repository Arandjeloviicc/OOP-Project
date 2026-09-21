package com.fittrack.cache;

import com.fittrack.dto.nutrition.food.FoodResponse;

import java.util.List;

public record CacheEntry(
        List<FoodResponse> foods,
        long cachedAt
) {}