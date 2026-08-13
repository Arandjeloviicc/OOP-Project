package com.fittrack.util;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public final class FitnessInputValidator {

    private FitnessInputValidator() {}

    public static boolean isHeightValid(String height) {
        if (height == null || !height.trim().matches("^\\d{1,3}(\\.\\d{1,2})?$")) {
            return false;
        }
        double value = Double.parseDouble(height.trim());
        return value >= AppConstants.Validation.MIN_HEIGHT && value <= AppConstants.Validation.MAX_HEIGHT;
    }

    public static boolean isWeightValid(String weight) {
        if (weight == null || !weight.trim().matches("^\\d{1,3}(\\.\\d{1,2})?$")) {
            return false;
        }
        double value = Double.parseDouble(weight.trim());
        return value >= AppConstants.Validation.MIN_WEIGHT && value <= AppConstants.Validation.MAX_WEIGHT;
    }

    public static boolean isAgeValid(String age) {
        if (age == null || !age.trim().matches("^\\d{1,3}$")) {
            return false;
        }

        int value = Integer.parseInt(age.trim());
        return value >= AppConstants.Validation.MIN_AGE
                && value <= AppConstants.Validation.MAX_AGE;
    }

    public static boolean isAgeValid(int age) {
        return age >= AppConstants.Validation.MIN_AGE
                && age <= AppConstants.Validation.MAX_AGE;
    }

    public static boolean isBodyFatValid(String bodyFat) {
        if (bodyFat == null || !bodyFat.matches("[1-9]\\d"))  {
            return false;
        }

        int bodyFatPercentage = Integer.parseInt(bodyFat);

        return bodyFatPercentage >= AppConstants.Validation.MIN_BODY_FAT_PERCENTAGE
                && bodyFatPercentage <= AppConstants.Validation.MAX_BODY_FAT_PERCENTAGE;
    }
}