package com.fittrack.backend.controller.nutrition;


import com.fittrack.backend.dto.nutrition.AddMealItemRequest;
import com.fittrack.backend.dto.nutrition.MealItemResponse;
import com.fittrack.backend.dto.nutrition.MealResponse;
import com.fittrack.backend.service.nutrition.MealService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/nutrition/meals")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @GetMapping("/user/{userId}")
    public List<MealResponse> getMealsForDate(@PathVariable Integer userId, @RequestParam LocalDate date) {
        return mealService.getMealsForDate(userId, date);
    }

    @PostMapping("/user/{userId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public MealItemResponse addMealItem(@PathVariable Integer userId, @Valid @RequestBody AddMealItemRequest request) {
        return mealService.addMealItem(userId, request);
    }
}
