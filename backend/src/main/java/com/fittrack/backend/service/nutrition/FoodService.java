package com.fittrack.backend.service.nutrition;

import com.fittrack.backend.dto.nutrition.food.CreateFoodRequest;
import com.fittrack.backend.dto.nutrition.food.FoodResponse;
import com.fittrack.backend.entity.nutrition.Food;
import com.fittrack.backend.repository.nutrition.food.FoodJdbcRepository;
import com.fittrack.backend.repository.nutrition.food.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final FoodJdbcRepository foodJdbcRepository;

    public List<Food> searchFoods(String search) {
        if (search == null || search.isBlank()) {
            return foodRepository.findTop20ByOrderByNameAsc();
        }

        return foodRepository.findTop20ByNameContainingIgnoreCaseOrderByNameAsc(search.trim());
    }

    public List<Food> getFoodsCreatedByUser(Integer userId, String search) {
        if (search == null || search.isBlank()) {
            return foodRepository.findByCreatedByUserIdOrderByNameAsc(userId);
        }

        return foodRepository.findByCreatedByUserIdAndNameContainingIgnoreCaseOrderByNameAsc(userId, search.trim());
    }

    public FoodResponse createFood(Integer userId, CreateFoodRequest request) {
        return foodJdbcRepository.createFood(
                userId,
                request
        );
    }

    public FoodResponse toResponse(Food food) {
        Integer createdByUserId = food.getCreatedByUser() != null
                ? food.getCreatedByUser().getId()
                : null;

        return new FoodResponse(
                food.getId(),
                food.getName(),
                food.getBrand(),
                food.getServingSizeGrams(),
                food.getCaloriesPerServing(),
                food.getProteinPerServing(),
                food.getCarbsPerServing(),
                food.getFatPerServing(),
                createdByUserId
        );
    }
}
