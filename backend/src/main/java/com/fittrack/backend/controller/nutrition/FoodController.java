package com.fittrack.backend.controller.nutrition;

import com.fittrack.backend.dto.nutrition.food.FoodRequest;
import com.fittrack.backend.dto.nutrition.food.FoodResponse;
import com.fittrack.backend.service.nutrition.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nutrition/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @GetMapping
    public List<FoodResponse> searchFoods(@RequestParam String search) {
        return foodService.searchFoods(search);
    }

    @GetMapping("/mine/{userId}")
    public List<FoodResponse> getMyFoods(@PathVariable Integer userId, @RequestParam(defaultValue = "") String search) {
        return foodService.getFoodsCreatedByUser(userId, search);
    }

    @PostMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FoodResponse createFood(@PathVariable Integer userId, @Valid @RequestBody FoodRequest request) {
        return foodService.createFood(userId, request);
    }

    @PutMapping("/user/{userId}/{foodId}")
    public FoodResponse updateFood(@PathVariable Integer userId, @PathVariable Integer foodId, @Valid @RequestBody FoodRequest request) {
        return foodService.updateFood(userId, foodId, request);
    }

    @DeleteMapping("/user/{userId}/{foodId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFood(@PathVariable Integer userId, @PathVariable Integer foodId) {
        foodService.deleteFood(userId, foodId);
    }
}