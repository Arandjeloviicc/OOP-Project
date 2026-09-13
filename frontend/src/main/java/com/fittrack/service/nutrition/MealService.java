package com.fittrack.service.nutrition;

import com.fittrack.api.nutrition.MealApi;
import com.fittrack.dto.nutrition.meal.*;
import com.fittrack.dto.nutrition.meal.item.AddMealItemRequest;
import com.fittrack.dto.nutrition.meal.item.MealItemResponse;
import com.fittrack.dto.nutrition.meal.item.UpdateMealItemRequest;
import com.fittrack.session.UserSession;

import java.time.LocalDate;
import java.util.List;

public class MealService {

    private final MealApi mealApi;

    public MealService() {
        this.mealApi = new MealApi();
    }

    // ── Daily meals ─────────────────────────────────────────────
    public List<MealResponse> getMealsForDate(LocalDate mealDate) {
        return mealApi.getMealsForDate(
                currentUserId(),
                mealDate
        );
    }

    public void addMealItem(AddMealItemRequest request) {
        mealApi.addMealItem(
                currentUserId(),
                request
        );
    }

    public MealItemResponse updateMealItem(Integer mealItemId, UpdateMealItemRequest request) {
        return mealApi.updateMealItem(
                currentUserId(),
                mealItemId,
                request
        );
    }

    public void deleteMealItem(Integer mealItemId) {
        mealApi.deleteMealItem(
                currentUserId(),
                mealItemId
        );
    }

    public boolean hasDailyMealItems(LocalDate date, String mealName) {
        return mealApi.hasDailyMealItems(
                currentUserId(),
                date,
                mealName
        );
    }

    public void copyDailyMeal(CopyMealRequest request) {
        mealApi.copyDailyMeal(
                currentUserId(),
                request
        );
    }

    // ── Saved meals ─────────────────────────────────────────────
    public List<MealResponse> searchMyMeals(String search) {
        return mealApi.getMyMeals(
                currentUserId(),
                search
        );
    }

    public void createSavedMeal(CreateMealRequest request) {
        mealApi.createSavedMeal(
                currentUserId(),
                request
        );
    }

    public void updateSavedMeal(Integer mealId, UpdateSavedMealRequest request) {
        mealApi.updateSavedMeal(
                currentUserId(),
                mealId,
                request
        );
    }

    public void deleteSavedMeal(Integer mealId) {
        mealApi.deleteSavedMeal(
                currentUserId(),
                mealId
        );
    }

    public void logSavedMeal(Integer mealId, LogSavedMealRequest request) {
        mealApi.logSavedMeal(
                currentUserId(),
                mealId,
                request
        );
    }

    // ── Helpers ─────────────────────────────────────────────
    private Integer currentUserId() {
        return UserSession.getInstance().getCurrentUser().id();
    }
}
