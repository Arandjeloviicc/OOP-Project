package com.fittrack.backend.service.nutrition;

import com.fittrack.backend.dto.nutrition.AddMealItemRequest;
import com.fittrack.backend.dto.nutrition.MealItemResponse;
import com.fittrack.backend.dto.nutrition.MealResponse;
import com.fittrack.backend.entity.nutrition.Food;
import com.fittrack.backend.entity.nutrition.Meal;
import com.fittrack.backend.entity.nutrition.MealItem;
import com.fittrack.backend.entity.user.User;
import com.fittrack.backend.repository.nutrition.FoodRepository;
import com.fittrack.backend.repository.nutrition.MealItemRepository;
import com.fittrack.backend.repository.nutrition.MealRepository;
import com.fittrack.backend.repository.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MealService {

    private final MealRepository mealRepository;
    private final MealItemRepository mealItemRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    public MealService(MealRepository mealRepository, MealItemRepository mealItemRepository, FoodRepository foodRepository, UserRepository userRepository) {
        this.mealRepository = mealRepository;
        this.mealItemRepository = mealItemRepository;
        this.foodRepository = foodRepository;
        this.userRepository = userRepository;
    }

    public List<MealResponse> getMealsForDate(Integer userId, LocalDate mealDate) {

        long start = System.currentTimeMillis();

        List<Meal> meals = mealRepository.findByUserIdAndMealDateWithItems(userId, mealDate);

        long afterQuery = System.currentTimeMillis();

        List<MealResponse> mealResponses = new ArrayList<>();

        for (Meal meal : meals) {
            mealResponses.add(toResponse(meal));
        }

        long end = System.currentTimeMillis();

        System.out.println(
                "DB query: " + (afterQuery - start) + " ms"
        );

        System.out.println(
                "Mapping: " + (end - afterQuery) + " ms"
        );

        return mealResponses;
    }

    public MealItemResponse addMealItem(Integer userId, AddMealItemRequest request) {
        Meal meal = mealRepository.findByUserIdAndMealDateAndName(
                userId,
                request.mealDate(),
                request.mealName()
        ).orElse(null);

        if (meal == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("User not found")
                    );

            meal = new Meal(user, request.mealName(), request.mealDate());
            meal = mealRepository.save(meal);
        }

        Food food = foodRepository.findById(request.foodId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Food not found")
                );

        MealItem mealItem = new MealItem(meal, food, request.quantityGrams());
        mealItem = mealItemRepository.save(mealItem);

        return toItemResponse(mealItem);
    }

    public MealResponse toResponse(Meal meal) {
        List<MealItem> mealItems = meal.getItems();

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
