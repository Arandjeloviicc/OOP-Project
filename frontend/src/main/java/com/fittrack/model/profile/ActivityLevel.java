package com.fittrack.model.profile;

public enum ActivityLevel {
    SEDENTARY("sedentary", 1.2),
    LIGHT("light", 1.375),
    MODERATE("moderate", 1.55),
    HEAVY("heavy", 1.725),
    ATHLETE("athlete", 1.9);

    private final String code;
    private final double multiplier;

    ActivityLevel(String code, double multiplier) {
        this.code = code;
        this.multiplier = multiplier;
    }

    public String getCode() {
        return code;
    }

    public double getMultiplier() { return multiplier; }

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