package com.fittrack.backend.service.nutrition;

import com.fittrack.backend.dto.nutrition.goal.NutritionTargets;
import com.fittrack.backend.repository.nutrition.goal.NutritionGoalJdbcRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class NutritionGoalService {

    private final NutritionGoalJdbcRepository nutritionGoalJdbcRepository;

    public NutritionGoalService(NutritionGoalJdbcRepository nutritionGoalJdbcRepository) {
        this.nutritionGoalJdbcRepository = nutritionGoalJdbcRepository;
    }

    public NutritionTargets getTargetsForDate(Integer userId, LocalDate date) {
        return nutritionGoalJdbcRepository
                .findTargetsForDate(userId, date)
                .orElseThrow(() ->
                        new IllegalArgumentException("Nutrition goal not found for selected date.")
                );
    }
}
