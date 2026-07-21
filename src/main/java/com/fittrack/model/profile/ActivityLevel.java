package com.fittrack.model.profile;

public enum ActivityLevel {
    SEDENTARY("sedentary"),
    LIGHT("light"),
    MODERATE("moderate"),
    HEAVY("heavy"),
    ATHLETE("athlete");

    private final String code;

    ActivityLevel(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return switch (this) {
            case SEDENTARY -> "Sedentary (office job)";
            case LIGHT -> "Light exercise (1-2 days/week)";
            case MODERATE -> "Moderate exercise (3-5 days/week)";
            case HEAVY -> "Heavy exercise (6-7 days/week)";
            case ATHLETE -> "Athlete (2x per day)";
        };
    }
}
