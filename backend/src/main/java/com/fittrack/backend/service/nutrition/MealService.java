package com.fittrack.backend.service.nutrition;

import com.fittrack.backend.dto.nutrition.meal.*;
import com.fittrack.backend.dto.nutrition.meal.item.*;
import com.fittrack.backend.entity.nutrition.Food;
import com.fittrack.backend.entity.nutrition.Meal;
import com.fittrack.backend.entity.nutrition.MealItem;
import com.fittrack.backend.entity.nutrition.MealKind;
import com.fittrack.backend.repository.nutrition.food.FoodRepository;
import com.fittrack.backend.repository.nutrition.meal.item.MealItemJdbcRepository;
import com.fittrack.backend.repository.nutrition.meal.item.MealItemRepository;
import com.fittrack.backend.repository.nutrition.meal.MealRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MealService {

    private final MealRepository mealRepository;
    private final MealItemRepository mealItemRepository;
    private final MealItemJdbcRepository mealItemJdbcRepository;
    private final FoodRepository foodRepository;

    public MealService(MealRepository mealRepository, MealItemRepository mealItemRepository, MealItemJdbcRepository mealItemJdbcRepository, FoodRepository foodRepository) {
        this.mealRepository = mealRepository;
        this.mealItemRepository = mealItemRepository;
        this.mealItemJdbcRepository = mealItemJdbcRepository;
        this.foodRepository = foodRepository;
    }

    public List<MealResponse> getMealsForDate(Integer userId, LocalDate mealDate) {

        List<Meal> meals = mealRepository.findByUserIdAndMealDateWithItems(userId, mealDate, MealKind.DAILY);

        List<MealResponse> mealResponses = new ArrayList<>();

        for (Meal meal : meals) {
            mealResponses.add(toResponse(meal));
        }

        return mealResponses;
    }

    @Transactional
    public void addMealItem(Integer userId, AddMealItemRequest request) {
        mealItemJdbcRepository.addDailyItem(
                userId,
                request.mealDate(),
                request.mealName(),
                request.foodId(),
                request.quantityGrams()
        );
    }

    @Transactional
    public MealItemResponse updateMealItem(Integer userId, Integer mealItemId, UpdateMealItemRequest request) {
        if (request.quantityGrams() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        String targetMealName = request.mealType().trim();

        return mealItemJdbcRepository.updateDailyItem(
                userId,
                mealItemId,
                targetMealName,
                request.quantityGrams()
        ).orElseThrow(() ->
                new IllegalArgumentException("Meal item not found or does not belong to user.")
        );
    }

    @Transactional
    public void deleteMealItem(Integer userId, Integer mealItemId) {
        int deleted = mealItemRepository.deleteByIdAndUserIdAndMealKind(mealItemId, userId, MealKind.DAILY);

        if (deleted == 0) {
            throw new IllegalArgumentException("Meal item not found or does not belong to user.");
        }
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
    public void createSavedMeal(Integer userId, CreateMealRequest request) {
        mealItemJdbcRepository.createSavedMeal(
                userId,
                request.name().trim(),
                request.items()
        );
    }

    @Transactional
    public void updateSavedMeal(Integer userId, Integer mealId, UpdateSavedMealRequest request) {
        Meal meal = mealRepository.findByIdWithItems(mealId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Meal not found")
                );

        if (meal.getKind() != MealKind.SAVED) {
            throw new IllegalArgumentException("Meal is not a saved meal.");
        }

        if (!meal.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Meal does not belong to user.");
        }

        meal.setName(request.name().trim());

        Map<Integer, MealItem> existingItemsById =
                meal.getItems()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        MealItem::getId,
                                        Function.identity()
                                )
                        );

        Map<Integer, Double> quantitiesToUpdate = new LinkedHashMap<>();

        Set<Integer> requestedExistingIds = new HashSet<>();

        for (UpdateSavedMealItemRequest itemRequest : request.items()) {
            if (itemRequest.mealItemId() == null) {
                continue;
            }

            MealItem existingItem =
                    existingItemsById.get(
                            itemRequest.mealItemId()
                    );

            if (existingItem == null) {
                throw new IllegalArgumentException("Meal item does not belong to meal.");
            }

            requestedExistingIds.add(
                    itemRequest.mealItemId()
            );

            if (Double.compare(
                    existingItem.getQuantityGrams(),
                    itemRequest.quantityGrams()
            ) != 0) {
                quantitiesToUpdate.put(
                        itemRequest.mealItemId(),
                        itemRequest.quantityGrams()
                );
            }
        }

        List<Integer> idsToDelete =
                existingItemsById.keySet()
                        .stream()
                        .filter(id ->
                                !requestedExistingIds.contains(id)
                        )
                        .toList();

        List<Integer> newFoodIds =
                request.items()
                        .stream()
                        .filter(item ->
                                item.mealItemId() == null
                        )
                        .map(UpdateSavedMealItemRequest::foodId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        Map<Integer, Food> foodsById =
                foodRepository.findAllById(newFoodIds)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        Food::getId,
                                        Function.identity()
                                )
                        );

        List<MealItem> newItems = new ArrayList<>();

        for (UpdateSavedMealItemRequest itemRequest : request.items()) {
            if (itemRequest.mealItemId() != null) {
                continue;
            }

            Food food = null;

            if (itemRequest.foodId() != null) {
                food = foodsById.get(
                        itemRequest.foodId()
                );

                if (food == null) {
                    throw new IllegalArgumentException("Food not found.");
                }
            }

            MealItem newItem = new MealItem(
                    meal,
                    food,
                    itemRequest.foodName().trim(),
                    itemRequest.brand(),
                    itemRequest.quantityGrams(),
                    itemRequest.servingSizeGrams(),
                    itemRequest.caloriesPerServing(),
                    itemRequest.proteinPerServing(),
                    itemRequest.carbsPerServing(),
                    itemRequest.fatPerServing()
            );

            newItems.add(newItem);
        }

        mealItemJdbcRepository.updateAndDelete(
                mealId,
                quantitiesToUpdate,
                idsToDelete
        );

        mealItemJdbcRepository.insertAll(
                mealId,
                newItems
        );
    }

    @Transactional
    public void deleteSavedMeal(Integer userId, Integer mealId) {
        int deleted = mealRepository.deleteByIdAndUserIdAndKind(
                mealId,
                userId,
                MealKind.SAVED
        );

        if (deleted == 0) {
            throw new IllegalArgumentException("Meal not found or does not belong to user.");
        }
    }

    @Transactional
    public void logSavedMeal(Integer userId, Integer mealId, LogSavedMealRequest request) {
        mealItemJdbcRepository.logSavedMeal(
                userId,
                mealId,
                request.mealDate(),
                request.mealName()
        );
    }

    @Transactional
    public void copyDailyMeal(Integer userId, CopyMealRequest request) {
        if (request.sourceDate().equals(request.targetDate())
            && request.sourceMealName().equals(request.targetMealName())) {
            throw new IllegalArgumentException("Source and target meal cannot be the same.");
        }

        mealItemJdbcRepository.copyDailyMeal(
                userId,
                request.sourceDate(),
                request.sourceMealName(),
                request.targetDate(),
                request.targetMealName()
        );
    }

    public boolean hasDailyMealItems(Integer userId, LocalDate mealDate, String mealName) {
        return mealItemRepository.existsDailyMealItems(
                userId,
                mealDate,
                mealName
        );
    }

    // ── Helpers ─────────────────────────────────────────────────
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
