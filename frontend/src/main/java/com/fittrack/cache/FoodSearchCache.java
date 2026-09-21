package com.fittrack.cache;

import com.fittrack.dto.nutrition.food.FoodResponse;
import java.util.Collections;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FoodSearchCache {

    // Cache Time To Live (TTL)
    private static final long CACHE_TTL_MS = 30_000;

    private static final Map<String, CacheEntry> CACHE = new ConcurrentHashMap<>();

    private FoodSearchCache() {}

    public static List<FoodResponse> get(String key) {
        CacheEntry entry = CACHE.get(key);

        if (entry == null) {
            return Collections.emptyList();
        }

        if (isExpired(entry)) {
            CACHE.remove(key, entry);
            return Collections.emptyList();
        }

        return entry.foods();
    }

    public static void put(String key, List<FoodResponse> foods) {
        CACHE.put(
                key,
                new CacheEntry(
                        foods,
                        System.currentTimeMillis()
                )
        );
    }

    public static boolean contains(String key) {
        CacheEntry entry = CACHE.get(key);

        if (entry == null) {
            return false;
        }

        if (isExpired(entry)) {
            CACHE.remove(key, entry);
            return false;
        }

        return true;
    }

    public static void clear() {
        CACHE.clear();
    }

    private static boolean isExpired(CacheEntry entry) {
        return System.currentTimeMillis() - entry.cachedAt() >= CACHE_TTL_MS;
    }
}