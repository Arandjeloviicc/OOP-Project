package com.fittrack.backend.service.nutrition;

import com.fittrack.backend.dto.nutrition.CreateFoodRequest;
import com.fittrack.backend.dto.nutrition.FoodResponse;
import com.fittrack.backend.entity.nutrition.Food;
import com.fittrack.backend.entity.user.User;
import com.fittrack.backend.repository.nutrition.FoodRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FoodService {

    private final FoodRepository foodRepository;

    public FoodService(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    public List<Food> searchFoods(String search) {

        if (search == null || search.isBlank()) {
            return foodRepository.findTop20ByOrderByNameAsc();
        }

        return foodRepository.findTop20ByNameContainingIgnoreCaseOrderByNameAsc(search.trim());
    }

    public List<Food> getFoodsCreatedByUser(Integer userId) {
        return foodRepository.findByCreatedByUser_IdOrderByNameAsc(userId);
    }

    public Food createFood(CreateFoodRequest request, User createdByUser) {
        String brand = request.getBrand();

        if (brand != null && brand.isBlank()) {
            brand = null;
        }

        Food food = new Food(
                request.getName().trim(),
                brand != null ? brand.trim() : null,
                request.getServingSizeGrams(),
                request.getCaloriesPerServing(),
                request.getProteinPerServing(),
                request.getCarbsPerServing(),
                request.getFatPerServing(),
                createdByUser
        );

        return foodRepository.save(food);
    }

    public Optional<Food> findById(Integer foodId) {
        return foodRepository.findById(foodId);
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
