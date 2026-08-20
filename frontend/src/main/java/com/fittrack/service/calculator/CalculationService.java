package com.fittrack.service.calculator;

import com.fittrack.model.profile.ActivityLevel;
import com.fittrack.model.profile.Gender;
import com.fittrack.config.AppConstants;

public final class CalculationService {

    // Ideal Body Fat Helpers
    private static final int[] IDEAL_BODY_FAT_AGES = {
            20, 25, 30, 35, 40, 45, 50, 55
    };

    private static final double[] IDEAL_BODY_FAT_MALE = {
            8.5, 10.5, 12.7, 13.7, 15.3, 16.4, 18.9, 20.9
    };

    private static final double[] IDEAL_BODY_FAT_FEMALE = {
            17.7, 18.4, 19.3, 21.5, 22.2, 22.9, 25.2, 26.3
    };

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
    public static double calculateBodyFatPercentage(Gender gender, double heightCm, double neckCm, double waistCm, Double hipCm) {
        if (gender == Gender.FEMALE) {
            return Math.max(0, calculateBodyFatFemale(heightCm, neckCm, waistCm, hipCm));
        }

        return Math.max(0, calculateBodyFatMale(heightCm, neckCm, waistCm));
    }

    private static double calculateBodyFatMale(double heightCm, double neckCm, double waistCm) {
        return (495 / (1.0324 - 0.19077 * Math.log10(waistCm - neckCm) + 0.15456 * Math.log10(heightCm))) - 450;
    }

    private static double calculateBodyFatFemale(double heightCm, double neckCm, double waistCm, double hipCm) {
        return (495 / (1.29579 - 0.35004 * Math.log10(waistCm + hipCm - neckCm) + 0.22100 * Math.log10(heightCm))) - 450;
    }

    public static double calculateFatMass(double bodyFatPercentage, double weightKg) {
        return (bodyFatPercentage / 100.0) * weightKg;
    }

    public static double calculateLeanMass(double weightKg, double fatMassKg) {
        return weightKg - fatMassKg;
    }

    public static double calculateIdealBodyFat(int age, Gender gender) {
        double[] values = gender == Gender.MALE
                ? IDEAL_BODY_FAT_MALE
                : IDEAL_BODY_FAT_FEMALE;

        if (age <= IDEAL_BODY_FAT_AGES[0]) {
            return values[0];
        }

        if (age >= IDEAL_BODY_FAT_AGES[IDEAL_BODY_FAT_AGES.length - 1]) {
            return values[values.length - 1];
        }

        for (int i = 0; i < IDEAL_BODY_FAT_AGES.length - 1; i++) {
            int lowerAge = IDEAL_BODY_FAT_AGES[i];
            int upperAge = IDEAL_BODY_FAT_AGES[i + 1];

            if (age >= lowerAge && age <= upperAge) {
                double lowerValue = values[i];
                double upperValue = values[i + 1];

                double progress = (double) (age - lowerAge) / (upperAge - lowerAge);

                return lowerValue + progress * (upperValue - lowerValue);
            }
        }

        return values[0];
    }

    public static double calculateBodyFatChange(double weightKg, double bodyFatPercentage, double idealBodyFatPercentage) {
        return weightKg * ((bodyFatPercentage - idealBodyFatPercentage) / 100.0);
    }
}