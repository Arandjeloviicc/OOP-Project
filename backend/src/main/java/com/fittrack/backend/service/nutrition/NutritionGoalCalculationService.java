package com.fittrack.backend.service.nutrition;

import com.fittrack.backend.dto.nutrition.goal.NutritionTargets;
import com.fittrack.backend.entity.profile.ActivityLevel;
import com.fittrack.backend.entity.profile.Gender;
import com.fittrack.backend.entity.profile.WeightGoal;
import org.springframework.stereotype.Service;

@Service
public class NutritionGoalCalculationService {

    // 7700 calories deficit/surplus to gain/lose 1kg of fat
    private static final double CALORIES_PER_KG = 7700.0;

    // Balanced Diet Macros
    private static final double CARBS_PERCENTAGE = 0.45;
    private static final double PROTEIN_PERCENTAGE = 0.25;
    private static final double FAT_PERCENTAGE = 0.30;

    // Calories / g for every macronutrient
    private static final double CALORIES_PER_GRAM_CARBS = 4.0;
    private static final double CALORIES_PER_GRAM_PROTEIN = 4.0;
    private static final double CALORIES_PER_GRAM_FAT = 9.0;

    // ── Nutrition Targets ─────────────────────────────────────────────────
    public NutritionTargets calculate(int age, Gender gender, double heightCm, double weightKg, Double bodyFatPercentage, ActivityLevel activityLevel, WeightGoal goalType, Double weeklyGoal) {
        if (bodyFatPercentage != null && (bodyFatPercentage < 2 || bodyFatPercentage > 70)) {
            throw new IllegalArgumentException("Body fat percentage must be between 0 and 100.");
        }

        double bmr = calculateBmr(
                age,
                gender,
                heightCm,
                weightKg,
                bodyFatPercentage
        );

        double tdee = calculateTdee(
                bmr,
                activityLevel
        );

        int targetCalories = calculateTargetCalories(
                tdee,
                goalType,
                weeklyGoal
        );

        double targetCarbs = targetCalories * CARBS_PERCENTAGE / CALORIES_PER_GRAM_CARBS;
        double targetFat = targetCalories * FAT_PERCENTAGE / CALORIES_PER_GRAM_FAT;
        double targetProtein = targetCalories * PROTEIN_PERCENTAGE / CALORIES_PER_GRAM_PROTEIN;

        return new NutritionTargets(
                targetCalories,
                round(targetCarbs),
                round(targetFat),
                round(targetProtein)
        );
    }

    // ── Bmr ─────────────────────────────────────────────────
    private static double calculateBmr(int age, Gender gender, double heightCm, double weightKg, Double bodyFatPercentage) {
        if (bodyFatPercentage != null) {
            return calculateBmrWithBodyFat(weightKg, bodyFatPercentage);
        }

        return calculateStandardBmr(age, gender, heightCm, weightKg);
    }

    private static double calculateBmrWithBodyFat(double weightKg, double bodyFatPercentage) {
        double leanBodyMass = weightKg * (1 - bodyFatPercentage / 100.0);

        return 370 + 21.6 * leanBodyMass;
    }

    private static double calculateStandardBmr(int age, Gender gender, double heightCm, double weightKg) {
        double bmrBase = 10*weightKg + 6.25*heightCm - 5*age;

        return (gender == Gender.MALE)
                ? bmrBase + 5
                : bmrBase - 161;
    }

    // ── Tdee ─────────────────────────────────────────────────
    private double calculateTdee(double bmr, ActivityLevel activityLevel) {
        return bmr * activityLevel.getMultiplier();
    }

    // ── Target Calories ─────────────────────────────────────────────────
    private int calculateTargetCalories(double tdee, WeightGoal goalType, Double weeklyGoal) {
        if (goalType == WeightGoal.MAINTAIN_WEIGHT) {
            return (int) Math.round(tdee);
        }

        int roundedCalories = calculateAdjustedCalories(tdee, goalType, weeklyGoal);

        if (roundedCalories <= 0) {
            throw new IllegalArgumentException("Calculated calorie target must be greater than zero.");
        }

        return roundedCalories;
    }

    private static int calculateAdjustedCalories(double tdee, WeightGoal goalType, Double weeklyGoal) {
        if (weeklyGoal == null || weeklyGoal <= 0) {
            throw new IllegalArgumentException("Weekly goal must be greater than zero for weight loss or weight gain.");
        }

        double dailyAdjustment = weeklyGoal * CALORIES_PER_KG / 7.0;

        double targetCalories = switch (goalType) {
            case LOSE_WEIGHT -> tdee - dailyAdjustment;
            case GAIN_WEIGHT -> tdee + dailyAdjustment;
            case MAINTAIN_WEIGHT -> tdee;
        };

        return (int) Math.round(targetCalories);
    }

    // ── Helpers ─────────────────────────────────────────────────
    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}