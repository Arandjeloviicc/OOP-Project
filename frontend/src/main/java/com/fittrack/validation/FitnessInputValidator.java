package com.fittrack.validation;

import com.fittrack.config.AppConstants;
import com.fittrack.util.NumberUtils;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public final class FitnessInputValidator {

    private FitnessInputValidator() {}

    private static boolean isDecimalMeasurementValid(String value, double min, double max) {
        if (value == null || !value.trim().matches("^\\d{1,3}([.,]\\d{1,2})?$")) {
            return false;
        }

        double parsedValue = NumberUtils.parseDecimal(value);
        return parsedValue >= min && parsedValue <= max;
    }

    public static boolean isHeightValid(String height) {
       return isDecimalMeasurementValid(height, AppConstants.Validation.MIN_HEIGHT, AppConstants.Validation.MAX_HEIGHT);
    }

    public static boolean isWeightValid(String weight) {
        return isDecimalMeasurementValid(weight, AppConstants.Validation.MIN_WEIGHT, AppConstants.Validation.MAX_WEIGHT);
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
        return isDecimalMeasurementValid(bodyFat, AppConstants.Validation.MIN_BODY_FAT_PERCENTAGE, AppConstants.Validation.MAX_BODY_FAT_PERCENTAGE);
    }

    public static boolean isNeckValid(String neck) {
        return isDecimalMeasurementValid(neck, AppConstants.Validation.MIN_NECK_CIRCUMFERENCE, AppConstants.Validation.MAX_NECK_CIRCUMFERENCE);
    }

    public static boolean isWaistValid(String waist) {
        return isDecimalMeasurementValid(waist, AppConstants.Validation.MIN_WAIST_CIRCUMFERENCE, AppConstants.Validation.MAX_WAIST_CIRCUMFERENCE);
    }

    public static boolean isHipValid(String hip) {
        return isDecimalMeasurementValid(hip, AppConstants.Validation.MIN_HIP_CIRCUMFERENCE, AppConstants.Validation.MAX_HIP_CIRCUMFERENCE);
    }

    public static boolean isNeckWaistRelationValid(double neck, double waist) {
        return neck < waist;
    }
}