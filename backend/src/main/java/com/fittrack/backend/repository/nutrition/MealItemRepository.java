package com.fittrack.backend.repository.nutrition;

import com.fittrack.backend.entity.nutrition.MealItem;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MealItemRepository extends JpaRepository<@NonNull MealItem, @NonNull Integer> {

    @Query("""
    SELECT mi
    FROM MealItem mi
    JOIN FETCH mi.meal
    WHERE mi.id = :mealItemId
    """)
    Optional<MealItem> findByIdWithMeal(
            @Param("mealItemId") Integer mealItemId
    );
}
