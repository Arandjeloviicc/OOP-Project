package com.fittrack.backend.repository.nutrition;

import com.fittrack.backend.entity.nutrition.MealItem;
import com.fittrack.backend.entity.nutrition.MealKind;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    @Query("""
        SELECT COUNT(mi)
        FROM MealItem mi
        WHERE mi.meal.user.id = :userId
          AND mi.meal.mealDate = :mealDate
          AND mi.meal.name = :mealName
          AND mi.meal.kind = :kind
        """)
    long countDailyMealItems(
            @Param("userId") Integer userId,
            @Param("mealDate") LocalDate mealDate,
            @Param("mealName") String mealName,
            @Param("kind") MealKind kind
    );
}
