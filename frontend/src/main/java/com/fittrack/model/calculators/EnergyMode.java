package com.fittrack.model.calculators;

import com.fittrack.model.profile.ActivityLevel;

public enum EnergyMode {
    BMR(null),
    SEDENTARY(ActivityLevel.SEDENTARY),
    LIGHT(ActivityLevel.LIGHT),
    MODERATE(ActivityLevel.MODERATE),
    HEAVY(ActivityLevel.HEAVY),
    ATHLETE(ActivityLevel.ATHLETE);

    private final ActivityLevel activityLevel;

    EnergyMode(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public boolean isBmr() {
        return this == BMR;
    }

    public String getShortName() {
        return switch (this) {
            case BMR -> "BMR";
            case SEDENTARY -> "Sedentary";
            case LIGHT -> "Light exercise";
            case MODERATE -> "Moderate exercise";
            case HEAVY -> "Heavy exercise";
            case ATHLETE -> "Athlete";
        };
    }

    @Override
    public String toString() {
        if (this == BMR) {
            return "Basal Metabolic Rate (BMR)";
        }
        return activityLevel.toString();
    }
}
