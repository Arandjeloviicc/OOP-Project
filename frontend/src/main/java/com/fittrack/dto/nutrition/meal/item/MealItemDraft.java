package com.fittrack.dto.nutrition.meal.item;

public record MealItemDraft(
        Integer mealItemId,
        Integer foodId,
        String foodName,
        String brand,
        double quantityGrams,
        double servingSizeGrams,
        double caloriesPerServing,
        double proteinPerServing,
        double carbsPerServing,
        double fatPerServing
) {
    public MealItemDraft withQuantity(double quantityGrams) {
        return new MealItemDraft(
                mealItemId,
                foodId,
                foodName,
                brand,
                quantityGrams,
                servingSizeGrams,
                caloriesPerServing,
                proteinPerServing,
                carbsPerServing,
                fatPerServing
        );
    }
}
