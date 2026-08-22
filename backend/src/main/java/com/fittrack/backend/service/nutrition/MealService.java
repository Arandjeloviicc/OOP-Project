package com.fittrack.backend.service.nutrition;

import com.fittrack.backend.dto.nutrition.meal.CreateMealRequest;
import com.fittrack.backend.dto.nutrition.meal.LogMealRequest;
import com.fittrack.backend.dto.nutrition.meal.item.AddMealItemRequest;
import com.fittrack.backend.dto.nutrition.meal.item.CreateMealItemRequest;
import com.fittrack.backend.dto.nutrition.meal.item.MealItemResponse;
import com.fittrack.backend.dto.nutrition.meal.MealResponse;
import com.fittrack.backend.dto.nutrition.meal.item.UpdateMealItemRequest;
import com.fittrack.backend.entity.nutrition.Food;
import com.fittrack.backend.entity.nutrition.Meal;
import com.fittrack.backend.entity.nutrition.MealItem;
import com.fittrack.backend.entity.nutrition.MealKind;
import com.fittrack.backend.entity.user.User;
import com.fittrack.backend.repository.nutrition.FoodRepository;
import com.fittrack.backend.repository.nutrition.MealItemRepository;
import com.fittrack.backend.repository.nutrition.MealRepository;
import com.fittrack.backend.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        List<Meal> meals = mealRepository.findByUserIdAndMealDateWithItems(userId, mealDate, MealKind.DAILY);

        List<MealResponse> mealResponses = new ArrayList<>();

        for (Meal meal : meals) {
            mealResponses.add(toResponse(meal));
        }

        return mealResponses;
    }

    public MealItemResponse addMealItem(Integer userId, AddMealItemRequest request) {
        Meal meal = mealRepository.findByUserIdAndMealDateAndNameAndKind(
                userId,
                request.mealDate(),
                request.mealName(),
                MealKind.DAILY
        ).orElse(null);

        if (meal == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("User not found")
                    );

            meal = new Meal(user, request.mealName(), request.mealDate(), MealKind.DAILY);
            meal = mealRepository.save(meal);
        }

        Food food = foodRepository.findById(request.foodId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Food not found")
                );

        if (food.getCreatedByUser() != null
                && !food.getCreatedByUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Food does not belong to user."
            );
        }

        MealItem mealItem = new MealItem(meal, food, request.quantityGrams());
        mealItem = mealItemRepository.save(mealItem);

        return toItemResponse(mealItem);
    }

    @Transactional
    public MealItemResponse updateMealItem(Integer userId, Integer mealItemId, UpdateMealItemRequest request) {
        MealItem mealItem = mealItemRepository.findById(mealItemId)
                .orElseThrow(() -> new IllegalArgumentException("Meal Item not found"));

        Meal currentMeal = mealItem.getMeal();

        if (currentMeal.getKind() != MealKind.DAILY) {
            throw new IllegalArgumentException(
                    "Meal item does not belong to a daily meal."
            );
        }

        if (!currentMeal.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Meal item does not belong to user.");
        }

        if (request.quantityGrams() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        mealItem.setQuantityGrams(request.quantityGrams());

        String requestedMealName = request.mealType();

        // If we change Meal
        if (!currentMeal.getName().equals(requestedMealName)) {
            Meal targetMeal = findOrCreateMeal(
                    currentMeal.getUser(),
                    currentMeal.getMealDate(),
                    requestedMealName
            );

            mealItem.setMeal(targetMeal);
        }

        MealItem savedItem = mealItemRepository.save(mealItem);

        return toItemResponse(savedItem);
    }

    @Transactional
    public void deleteMealItem(Integer userId, Integer mealItemId) {
        MealItem mealItem = mealItemRepository.findById(mealItemId)
                .orElseThrow(() ->  new IllegalArgumentException("Meal Item not found"));

        Meal currentMeal = mealItem.getMeal();

        if (currentMeal.getKind() != MealKind.DAILY) {
            throw new IllegalArgumentException(
                    "Meal item does not belong to a daily meal."
            );
        }

        if (!mealItem.getMeal().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Meal item does not belong to user."
            );
        }

        mealItemRepository.delete(mealItem);
    }

    public List<MealResponse> getMyMeals(Integer userId, String search) {
        List<Meal> meals;

        if (search == null || search.isBlank()) {
            meals = mealRepository.findByUserIdAndKindWithItems(userId, MealKind.SAVED);
        } else {
            meals = mealRepository.findByUserIdAndKindAndNameContainingWithItems(userId, MealKind.SAVED, search.trim());
        }

        List<MealResponse> responses = new ArrayList<>();

        for (Meal meal : meals) {
            responses.add(toResponse(meal));
        }

        return responses;
    }

    @Transactional
    public MealResponse createMyMeal(Integer userId, CreateMealRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Meal meal = new Meal(
                user,
                request.name().trim(),
                null,
                MealKind.SAVED
        );

        meal = mealRepository.save(meal);

        for (CreateMealItemRequest itemRequest : request.items()) {
            Food food = foodRepository.findById(itemRequest.foodId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Food not found")
                    );

            if (food.getCreatedByUser() != null
                && !food.getCreatedByUser().getId().equals(userId)) {
                throw new IllegalArgumentException(
                        "Food does not belong to user."
                );
            }

            MealItem mealItem = new MealItem(
                    meal,
                    food,
                    itemRequest.quantityGrams()
            );

            mealItemRepository.save(mealItem);
            meal.getItems().add(mealItem);
        }

        return toResponse(meal);
    }

    @Transactional
    public void logMyMeal(Integer userId, Integer mealId, LogMealRequest request) {
        Meal savedMeal = mealRepository.findById(mealId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Meal not found")
                );

        if (savedMeal.getKind() != MealKind.SAVED) {
            throw new IllegalArgumentException(
                    "Meal is not a saved meal."
            );
        }

        if (!savedMeal.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Meal does not belong to user."
            );
        }

        Meal targetMeal = findOrCreateMeal(
                savedMeal.getUser(),
                request.mealDate(),
                request.mealName()
        );

        for (MealItem savedItem : savedMeal.getItems()) {
            MealItem mealItem = new MealItem(
                    targetMeal,
                    savedItem
            );

            mealItemRepository.save(mealItem);
        }
    }

    // ── Helpers ─────────────────────────────────────────────────
    private Meal findOrCreateMeal(User user, LocalDate mealDate, String mealName) {
        return mealRepository.findByUserIdAndMealDateAndNameAndKind(user.getId(), mealDate, mealName, MealKind.DAILY)
                .orElseGet(() -> mealRepository.save(new Meal(user, mealName, mealDate, MealKind.DAILY)));
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
                mealItem.getFood() != null ? mealItem.getFood().getId() : null,
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
