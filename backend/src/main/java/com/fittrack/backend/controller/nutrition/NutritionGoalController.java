package com.fittrack.backend.controller.nutrition;

import com.fittrack.backend.dto.nutrition.goal.NutritionTargets;
import com.fittrack.backend.service.nutrition.NutritionGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/nutrition/goals")
@RequiredArgsConstructor
public class NutritionGoalController {

    private final NutritionGoalService nutritionGoalService;

    @GetMapping("/user/{userId}/targets")
    public NutritionTargets getTargetsForDate(@PathVariable Integer userId, @RequestParam LocalDate date) {
        return nutritionGoalService.getTargetsForDate(userId, date);
    }
}