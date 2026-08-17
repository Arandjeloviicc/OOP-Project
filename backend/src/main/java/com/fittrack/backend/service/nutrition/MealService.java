package com.fittrack.backend.service.nutrition;

import com.fittrack.backend.dto.nutrition.MealItemResponse;
import com.fittrack.backend.dto.nutrition.MealResponse;
import com.fittrack.backend.entity.nutrition.Meal;
import com.fittrack.backend.entity.nutrition.MealItem;
import com.fittrack.backend.repository.nutrition.MealItemRepository;
import com.fittrack.backend.repository.nutrition.MealRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MealService {

    private final MealRepository mealRepository;
    private final MealItemRepository mealItemRepository;

    public MealService(MealRepository mealRepository, MealItemRepository mealItemRepository) {
        this.mealRepository = mealRepository;
        this.mealItemRepository = mealItemRepository;
    }

    public List<MealResponse> getMealsForDate(Integer userId, LocalDate mealDate) {
        List<Meal> meals = mealRepository.findByUser_IdAndMealDateOrderByIdAsc(userId, mealDate);

        List<MealResponse> mealResponses = new ArrayList<>();

        for (Meal meal : meals) {
            mealResponses.add(toResponse(meal));
        }

        return mealResponses;
    }

    public MealResponse toResponse(Meal meal) {
        List<MealItem> mealItems = mealItemRepository.findByMealIdOrderByIdAsc(meal.getId());

        List<MealItemResponse> items = new ArrayList<>();

        for (MealItem mealItem : mealItems) {
            items.add(toItemResponse(mealItem));
        }

        return new MealResponse(
            meal.getId(),
            meal.getName(),
            meal.getMealDate(),
            items
        );
    }

    public MealItemResponse toItemResponse(MealItem mealItem) {
        return new MealItemResponse(
                mealItem.getId(),
                mealItem.getFoodName(),
                mealItem.getBrand(),
                mealItem.getQuantityGrams(),
                mealItem.getServingSizeGrams(),
                mealItem.getCaloriesPerServing(),
                mealItem.getProteinPerServing(),
                mealItem.getCarbsPerServing(),
                mealItem.getFatPerServing()
        );
    }
}
