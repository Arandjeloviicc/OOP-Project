package com.fittrack.backend.service.nutrition;

import com.fittrack.backend.dto.nutrition.food.FoodRequest;
import com.fittrack.backend.dto.nutrition.food.FoodResponse;
import com.fittrack.backend.repository.nutrition.food.FoodJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodJdbcRepository foodJdbcRepository;

    public List<FoodResponse> searchFoods(String search) {
        if (search == null || search.isBlank()) {
            return foodJdbcRepository.findTop20OrderByName();
        }

        return foodJdbcRepository.findTop20ByNameContaining(search.trim());
    }

    public List<FoodResponse> getFoodsCreatedByUser(Integer userId, String search) {
        if (search == null || search.isBlank()) {
            return foodJdbcRepository.findByUserIdOrderByName(userId);
        }

        return foodJdbcRepository.findByUserIdAndNameContaining(userId, search.trim());
    }

    public FoodResponse createFood(Integer userId, FoodRequest request) {
        return foodJdbcRepository.createFood(userId, request);
    }

    public FoodResponse updateFood(Integer userId, Integer foodId, FoodRequest request) {
        return foodJdbcRepository.updateFood(userId, foodId, request);
    }

    public void deleteFood(Integer userId, Integer foodId) {
        foodJdbcRepository.deleteFood(userId, foodId);
    }
}
