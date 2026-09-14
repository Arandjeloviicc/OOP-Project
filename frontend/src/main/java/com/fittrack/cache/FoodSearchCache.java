package com.fittrack.cache;

import com.fittrack.dto.nutrition.food.FoodResponse;
import java.util.Collections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FoodSearchCache {

    // Cash TimeToLive (TTL)
    private static final long CACHE_TTL_MS = 30_000;

    private static final Map<String, List<FoodResponse>> CACHE = new HashMap<>();
    private static final Map<String, Long> CACHE_TIMESTAMPS = new HashMap<>();

    private FoodSearchCache() {}

    public static List<FoodResponse> get(String key) {
        if (!contains(key)) {
            return Collections.emptyList();
        }

        return CACHE.get(key);
    }

    public static void put(String key, List<FoodResponse> foods) {
        CACHE.put(key, foods);
        CACHE_TIMESTAMPS.put(key, System.currentTimeMillis());
    }

    public static boolean contains(String key) {
        if (!CACHE.containsKey(key)) {
            return false;
        }

        Long cachedAt = CACHE_TIMESTAMPS.get(key);

        if (cachedAt == null) {
            remove(key);
            return false;
        }

        boolean expired = System.currentTimeMillis() - cachedAt >= CACHE_TTL_MS;

        if (expired) {
            remove(key);
            return false;
        }

        return true;
    }

    public static void clear() {
        CACHE.clear();
        CACHE_TIMESTAMPS.clear();
    }

    private static void remove(String key) {
        CACHE.remove(key);
        CACHE_TIMESTAMPS.remove(key);
    }
}