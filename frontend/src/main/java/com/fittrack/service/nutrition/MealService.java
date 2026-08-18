package com.fittrack.service.nutrition;

import com.fittrack.dto.nutrition.MealItemResponse;
import com.fittrack.dto.nutrition.MealResponse;
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

    public static int calculateMealCalories(MealResponse meal) {
        double totalCalories = 0;

        for (MealItemResponse item : meal.items()) {
            double servings = item.quantityGrams() / item.servingSizeGrams();
            totalCalories += servings * item.caloriesPerServing();
        }

        return (int) Math.round(totalCalories);
    }
}
