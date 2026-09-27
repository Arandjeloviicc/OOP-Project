package com.fittrack.backend.service.nutrition;

import com.fittrack.backend.dto.nutrition.meal.*;
import com.fittrack.backend.dto.nutrition.meal.item.*;
import com.fittrack.backend.domain.nutrition.MealKind;
import com.fittrack.backend.exception.ResourceNotFoundException;
import com.fittrack.backend.repository.nutrition.meal.MealJdbcRepository;
import com.fittrack.backend.repository.nutrition.meal.item.MealItemJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MealService {

    private final MealJdbcRepository mealJdbcRepository;
    private final MealItemJdbcRepository mealItemJdbcRepository;

    public List<MealResponse> getMealsForDate(Integer userId, LocalDate mealDate) {
        return mealJdbcRepository.findByUserIdAndMealDate(
                userId,
                mealDate,
                MealKind.DAILY
        );
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
                new ResourceNotFoundException("Meal item not found or does not belong to user.")
        );
    }

    @Transactional
    public void deleteMealItem(Integer userId, Integer mealItemId) {
        int deleted = mealItemJdbcRepository.deleteDailyItem(userId, mealItemId);

        if (deleted == 0) {
            throw new ResourceNotFoundException("Meal item not found or does not belong to user.");
        }
    }

    public List<MealResponse> getMyMeals(Integer userId, String search) {
        if (search == null || search.isBlank()) {
            return mealJdbcRepository.findByUserIdAndKind(
                    userId,
                    MealKind.SAVED
            );
        }

        return mealJdbcRepository.findByUserIdAndKindAndNameContaining(
                userId,
                MealKind.SAVED,
                search.trim()
        );
    }

    @Transactional
    public void createSavedMeal(Integer userId, CreateMealRequest request) {
        mealJdbcRepository.createSavedMeal(
                userId,
                request.name().trim(),
                request.items()
        );
    }

    @Transactional
    public void updateSavedMeal(Integer userId, Integer mealId, UpdateSavedMealRequest request) {
        mealJdbcRepository.updateSavedMeal(
                userId,
                mealId,
                request
        );
    }

    @Transactional
    public void deleteSavedMeal(Integer userId, Integer mealId) {
        int deleted = mealJdbcRepository.deleteByIdAndUserIdAndKind(
                mealId,
                userId,
                MealKind.SAVED
        );

        if (deleted == 0) {
            throw new ResourceNotFoundException("Meal not found or does not belong to user.");
        }
    }

    @Transactional
    public void logSavedMeal(Integer userId, Integer mealId, LogSavedMealRequest request) {
        mealJdbcRepository.logSavedMeal(
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

        mealJdbcRepository.copyDailyMeal(
                userId,
                request.sourceDate(),
                request.sourceMealName(),
                request.targetDate(),
                request.targetMealName()
        );
    }

    public boolean hasDailyMealItems(Integer userId, LocalDate mealDate, String mealName) {
        return mealJdbcRepository.existsDailyMealItems(
                userId,
                mealDate,
                mealName
        );
    }
}