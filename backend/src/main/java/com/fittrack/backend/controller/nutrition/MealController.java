package com.fittrack.backend.controller.nutrition;


import com.fittrack.backend.dto.nutrition.meal.*;
import com.fittrack.backend.dto.nutrition.meal.item.AddMealItemRequest;
import com.fittrack.backend.dto.nutrition.meal.item.MealItemResponse;
import com.fittrack.backend.dto.nutrition.meal.item.UpdateMealItemRequest;
import com.fittrack.backend.service.nutrition.MealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/nutrition/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;

    @GetMapping("/user/{userId}")
    public List<MealResponse> getMealsForDate(@PathVariable Integer userId, @RequestParam LocalDate date) {
        return mealService.getMealsForDate(userId, date);
    }

    @PostMapping("/user/{userId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public void addMealItem(@PathVariable Integer userId, @Valid @RequestBody AddMealItemRequest request) {
        mealService.addMealItem(userId, request);
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

    @PostMapping("/mine/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void createSavedMeal(@PathVariable Integer userId, @Valid @RequestBody CreateMealRequest request) {
        mealService.createSavedMeal(userId, request);
    }

    @PutMapping("/mine/{userId}/{mealId}")
    public void updateSavedMeal(@PathVariable Integer userId, @PathVariable Integer mealId, @Valid @RequestBody UpdateSavedMealRequest request) {
        mealService.updateSavedMeal(userId, mealId, request);
    }

    @DeleteMapping("/mine/{userId}/{mealId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSavedMeal(@PathVariable Integer userId, @PathVariable Integer mealId) {
        mealService.deleteSavedMeal(userId, mealId);
    }

    @PostMapping("/mine/{userId}/{mealId}/log")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logSavedMeal(@PathVariable Integer userId, @PathVariable Integer mealId, @Valid @RequestBody LogSavedMealRequest request) {
        mealService.logSavedMeal(userId, mealId, request);
    }

    @PostMapping("/user/{userId}/copy")
    public void copyDailyMeal(@PathVariable Integer userId, @Valid @RequestBody CopyMealRequest request) {
        mealService.copyDailyMeal(userId, request);
    }

    @GetMapping("/user/{userId}/has-items")
    public boolean hasDailyMealItems(@PathVariable Integer userId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @RequestParam String mealName) {
        return mealService.hasDailyMealItems(userId, date, mealName);
    }
}