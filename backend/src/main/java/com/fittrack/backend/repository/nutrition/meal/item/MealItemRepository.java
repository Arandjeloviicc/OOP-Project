package com.fittrack.backend.repository.nutrition.meal.item;

import com.fittrack.backend.entity.nutrition.MealItem;
import com.fittrack.backend.entity.nutrition.MealKind;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface MealItemRepository extends JpaRepository<@NonNull MealItem, @NonNull Integer> {

    @Query(value = """
    SELECT EXISTS (
        SELECT 1
        FROM meal_items mi
        JOIN meals m
            ON m.id = mi.meal_id
        WHERE m.user_id = :userId
          AND m.meal_date = :mealDate
          AND m.name = :mealName
          AND m.kind = 'DAILY'
    )
    """, nativeQuery = true
    )
    boolean existsDailyMealItems(
            @Param("userId") Integer userId,
            @Param("mealDate") LocalDate mealDate,
            @Param("mealName") String mealName
    );

    @Modifying
    @Query("""
    DELETE FROM MealItem mi
    WHERE mi.id = :mealItemId
        AND mi.meal.user.id = :userId
        AND mi.meal.kind = :mealKind
    """)
    int deleteByIdAndUserIdAndMealKind(
            @Param("mealItemId") Integer mealItemId,
            @Param("userId") Integer userId,
            @Param("mealKind") MealKind mealKind
    );
}