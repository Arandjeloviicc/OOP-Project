package com.fittrack.backend.controller.nutrition;

import com.fittrack.backend.dto.nutrition.CreateFoodRequest;
import com.fittrack.backend.dto.nutrition.FoodResponse;
import com.fittrack.backend.entity.nutrition.Food;
import com.fittrack.backend.entity.user.User;
import com.fittrack.backend.repository.user.UserRepository;
import com.fittrack.backend.service.nutrition.FoodService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private final FoodService foodService;
    private final UserRepository userRepository;

    public FoodController(FoodService foodService, UserRepository userRepository) {
        this.foodService = foodService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<FoodResponse> searchFoods(@RequestParam String search) {
        return foodService.searchFoods(search)
                .stream()
                .map(foodService::toResponse)
                .toList();
    }

    @GetMapping("/mine/{userId}")
    public List<FoodResponse> getMyFoods(@PathVariable Integer userId) {
        return foodService.getFoodsCreatedByUser(userId)
                .stream()
                .map(foodService::toResponse)
                .toList();
    }

    @PostMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FoodResponse createFood(@PathVariable Integer userId, @Valid @RequestBody CreateFoodRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        Food food = foodService.createFood(request, user);

        return foodService.toResponse(food);
    }
}