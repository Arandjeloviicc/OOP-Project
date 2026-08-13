package com.fittrack.service.calculator;

import com.fittrack.model.profile.ActivityLevel;
import com.fittrack.model.profile.Gender;
import com.fittrack.util.AppConstants;

public final class CalculationService {

    private CalculationService() {}

    // ── BMI ─────────────────────────────────────────────────
    public static double calculateBmi(double heightMeters, double weightKg) {
        double heightSquared = heightMeters * heightMeters;
        double exactBmi = weightKg / heightSquared;

        return Math.round(exactBmi * 10.0) / 10.0;
    }

    public static double calculateHealthyWeightMin(double heightMeters) {
        double heightSquared = heightMeters * heightMeters;

        return 18.5 * heightSquared;
    }

    public static double calculateHealthyWeightMax(double heightMeters) {
        double heightSquared = heightMeters * heightMeters;

        return 24.9 * heightSquared;
    }

    public static double calculateBmiPrime(double bmi) {
        return bmi / 25;
    }

    public static double calculatePonderalIndex(double heightMeters, double weightKg) {
        double heightCubed = heightMeters * heightMeters * heightMeters;

        return weightKg / heightCubed;
    }

    // ── BMR ─────────────────────────────────────────────────
    public static double calculateBmr(int age, Gender gender, double heightCm, double weightKg, Double bodyFatPercentage) {
        if (bodyFatPercentage != null) {
            return calculateBmrWithBodyFat(weightKg, bodyFatPercentage);
        }

        return calculateStandardBmr(age, gender, heightCm, weightKg);
    }

    public static double calculateBmrWithBodyFat(double weightKg, double bodyFatPercentage) {
        double leanBodyMass = weightKg * (1 - bodyFatPercentage / 100.0);

        return 370 + 21.6 * leanBodyMass;
    }

    public static double calculateStandardBmr(int age, Gender gender, double heightCm, double weightKg) {
        double bmrBase = 10*weightKg + 6.25*heightCm - 5*age;

        return (gender == Gender.MALE)
                ? bmrBase + 5
                : bmrBase - 161;
    }

    // ── TDEE ─────────────────────────────────────────────────
    public static double calculateTdee(double bmr, ActivityLevel activityLevel) {
        return bmr * activityLevel.getMultiplier();
    }

    public static int calculateWeightLossCalories(int tdee) {
        return tdee - AppConstants.Validation.WEIGHT_LOSS_CALORIE_DEFICIT;
    }

    public static int calculateWeightGainCalories(int tdee) {
        return tdee + AppConstants.Validation.WEIGHT_GAIN_CALORIE_SURPLUS;
    }

    // ── Body fat ─────────────────────────────────────────────────
}
