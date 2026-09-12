package com.fittrack.backend.controller.nutrition;

import com.fittrack.backend.dto.nutrition.food.CreateFoodRequest;
import com.fittrack.backend.dto.nutrition.food.FoodResponse;
import com.fittrack.backend.entity.nutrition.Food;
import com.fittrack.backend.service.nutrition.FoodService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/nutrition/foods")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping
    public List<FoodResponse> searchFoods(@RequestParam String search) {
        List<Food> foods = foodService.searchFoods(search);

        List<FoodResponse> responses = new ArrayList<>();

        for (Food food : foods) {
            responses.add(foodService.toResponse(food));
        }

        return responses;
    }

    @GetMapping("/mine/{userId}")
    public List<FoodResponse> getMyFoods(@PathVariable Integer userId, @RequestParam(defaultValue = "") String search) {
        List<Food> foods = foodService.getFoodsCreatedByUser(userId, search);

        List<FoodResponse> responses = new ArrayList<>();

        for (Food food : foods) {
            responses.add(foodService.toResponse(food));
        }

        return responses;
    }

    @PostMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FoodResponse createFood(@PathVariable Integer userId, @Valid @RequestBody CreateFoodRequest request) {
        return foodService.createFood(userId, request);
    }
}