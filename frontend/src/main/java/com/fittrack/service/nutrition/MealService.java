package com.fittrack.service.nutrition;

import com.fittrack.dto.nutrition.meal.item.MealItemDraft;
import com.fittrack.dto.nutrition.meal.item.MealItemResponse;
import com.fittrack.dto.nutrition.meal.MealResponse;
import com.fittrack.model.nutrition.DailyNutritionTotals;

import java.util.List;

public final class MealService {

    private MealService() {}

    public static DailyNutritionTotals calculateDailyNutritionTotals(List<MealResponse> meals) {
        double calories = 0;
        double carbs = 0;
        double fat = 0;
        double protein = 0;

        for (MealResponse meal : meals) {
            for (MealItemResponse item : meal.items()) {
                double servings = item.quantityGrams() / item.servingSizeGrams();

                calories += servings * item.caloriesPerServing();

                carbs += servings * item.carbsPerServing();
                fat += servings * item.fatPerServing();
                protein += servings * item.proteinPerServing();
            }
        }

        return new DailyNutritionTotals(calories, carbs, fat, protein);
    }

    public static DailyNutritionTotals calculateMealNutritionTotals(MealResponse meal) {
        if (meal == null || meal.items().isEmpty()) {
            return new DailyNutritionTotals(0, 0, 0, 0);
        }

        return calculateDailyNutritionTotals(List.of(meal));
    }

    public static int calculateMealCalories(MealResponse meal) {
        double totalCalories = 0;

        for (MealItemResponse item : meal.items()) {
            double servings = item.quantityGrams() / item.servingSizeGrams();
            totalCalories += servings * item.caloriesPerServing();
        }

        return (int) Math.round(totalCalories);
    }

    public static double calculateFoodCalories(MealItemResponse mealItem) {
        if (mealItem.servingSizeGrams() <= 0) {
            return 0.0;
        }

        return mealItem.quantityGrams() / mealItem.servingSizeGrams() * mealItem.caloriesPerServing();
    }

    public static List<MealItemDraft> createDraftItems(MealResponse meal) {
        return meal.items()
                .stream()
                .map(item -> new MealItemDraft(
                        null,
                        item.foodId(),
                        item.foodName(),
                        item.brand(),
                        item.quantityGrams(),
                        item.servingSizeGrams(),
                        item.caloriesPerServing(),
                        item.proteinPerServing(),
                        item.carbsPerServing(),
                        item.fatPerServing()
                ))
                .toList();
    }
}
