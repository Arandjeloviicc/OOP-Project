package com.fittrack.cache;

import com.fittrack.dto.nutrition.food.FoodResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FoodSearchCache {

    private static final Map<String, List<FoodResponse>> CACHE = new HashMap<>();

    private FoodSearchCache() {}

    public static List<FoodResponse> get(String key) {
        return CACHE.get(key);
    }

    public static void put(String key, List<FoodResponse> foods) {
        CACHE.put(key, foods);
    }

    public static boolean contains(String key) {
        return CACHE.containsKey(key);
    }

    public static void clear() {
        CACHE.clear();
    }
}