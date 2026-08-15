package com.fittrack.backend.repository.nutrition;

import com.fittrack.backend.entity.nutrition.MealItem;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MealItemRepository extends JpaRepository<@NonNull MealItem, @NonNull Integer> {

    List<MealItem> findByMealIdOrderByIdAsc(Integer mealId);

    Optional<MealItem> findByIdAndMealId(Integer itemId, Integer mealId);
}
