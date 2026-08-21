package com.fittrack.backend.controller.nutrition;


import com.fittrack.backend.dto.nutrition.AddMealItemRequest;
import com.fittrack.backend.dto.nutrition.MealItemResponse;
import com.fittrack.backend.dto.nutrition.MealResponse;
import com.fittrack.backend.dto.nutrition.UpdateMealItemRequest;
import com.fittrack.backend.service.nutrition.MealService;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PutMapping("/user/{userId}/items/{mealItemId}")
    public ResponseEntity<@NonNull MealItemResponse> updateMealItem(@PathVariable Integer userId, @PathVariable Integer mealItemId, @Valid @RequestBody UpdateMealItemRequest request) {
        return ResponseEntity.ok(mealService.updateMealItem(userId, mealItemId, request));
    }

    @DeleteMapping("/user/{userId}/items/{mealItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMealItem(@PathVariable Integer userId, @PathVariable Integer mealItemId) {
        mealService.deleteMealItem(userId, mealItemId);
    }

    @GetMapping("/mine/{userId}")
    public List<MealResponse> getMyMeals(@PathVariable Integer userId, @RequestParam(defaultValue = "") String search) {
        return mealService.getMyMeals(userId, search);
    }
}
