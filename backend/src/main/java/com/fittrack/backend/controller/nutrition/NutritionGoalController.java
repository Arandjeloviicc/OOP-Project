package com.fittrack.backend.controller.nutrition;

import com.fittrack.backend.dto.nutrition.goal.NutritionTargets;
import com.fittrack.backend.service.nutrition.NutritionGoalService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/nutrition/goals")
public class NutritionGoalController {

    private final NutritionGoalService nutritionGoalService;

    public NutritionGoalController(NutritionGoalService nutritionGoalService) {
        this.nutritionGoalService = nutritionGoalService;
    }

    @GetMapping("/user/{userId}/targets")
    public NutritionTargets getTargetsForDate(@PathVariable Integer userId, @RequestParam LocalDate date) {
        return nutritionGoalService.getTargetsForDate(userId, date);
    }
}