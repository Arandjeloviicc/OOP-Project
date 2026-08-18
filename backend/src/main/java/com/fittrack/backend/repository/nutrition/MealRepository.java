package com.fittrack.backend.repository.nutrition;

import com.fittrack.backend.entity.nutrition.Meal;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MealRepository extends JpaRepository<@NonNull Meal, @NonNull Integer> {

    @Query("""
        SELECT DISTINCT m
        FROM Meal m
        LEFT JOIN FETCH m.items
        WHERE m.user.id = :userId
        AND m.mealDate = :mealDate
        ORDER BY m.id
        """)
    List<Meal> findByUserIdAndMealDateWithItems(
            @Param("userId") Integer userId,
            @Param("mealDate") LocalDate mealDate
    );

    Optional<Meal> findByUserIdAndMealDateAndName(Integer userId, LocalDate mealDate, String name);
}