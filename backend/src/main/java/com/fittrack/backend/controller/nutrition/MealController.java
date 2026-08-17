package com.fittrack.backend.controller.nutrition;


import com.fittrack.backend.dto.nutrition.MealResponse;
import com.fittrack.backend.service.nutrition.MealService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/nutrition")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @GetMapping("/user/{userId}")
    public List<MealResponse> getMealsForDate(@PathVariable Integer userId, @RequestParam LocalDate date) {
        return mealService.getMealsForDate(userId, date);
    }
}
