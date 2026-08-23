package com.fittrack.backend.repository.nutrition;

import com.fittrack.backend.entity.nutrition.Meal;
import com.fittrack.backend.entity.nutrition.MealKind;
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
    AND m.kind = :kind
    ORDER BY m.id
    """)
    List<Meal> findByUserIdAndMealDateWithItems(
            @Param("userId") Integer userId,
            @Param("mealDate") LocalDate mealDate,
            @Param("kind") MealKind kind
    );

    Optional<Meal> findByUserIdAndMealDateAndNameAndKind(Integer userId, LocalDate mealDate, String name, MealKind kind);

    @Query("""
    SELECT DISTINCT m
    FROM Meal m
    LEFT JOIN FETCH m.items
    WHERE m.user.id = :userId
    AND m.kind = :kind
    ORDER BY m.name
    """)
    List<Meal> findByUserIdAndKindWithItems(
            @Param("userId") Integer userId,
            @Param("kind") MealKind kind
    );

    @Query("""
    SELECT DISTINCT m
    FROM Meal m
    LEFT JOIN FETCH m.items
    WHERE m.user.id = :userId
    AND m.kind = :kind
    AND LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%'))
    ORDER BY m.name
    """)
    List<Meal> findByUserIdAndKindAndNameContainingWithItems(
            @Param("userId") Integer userId,
            @Param("kind") MealKind kind,
            @Param("search") String search
    );

    @Query("""
    SELECT DISTINCT m
    FROM Meal m
    LEFT JOIN FETCH m.items
    WHERE m.id = :mealId
    """)
    Optional<Meal> findByIdWithItems(
            @Param("mealId") Integer mealId
    );
}