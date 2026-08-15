package com.fittrack.backend.repository.nutrition;

import com.fittrack.backend.entity.nutrition.Meal;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MealRepository extends JpaRepository<@NonNull Meal, @NonNull Integer> {

    List<Meal> findByUser_IdAndMealDateOrderByIdAsc(Integer userId, LocalDate mealDate);

    Optional<Meal> findByIdAndUser_Id(Integer mealId, Integer userId);
}
