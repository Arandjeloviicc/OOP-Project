package com.fittrack.model.profile;

public enum ActivityLevel {
    SEDENTARY("Sedentary (office job)", "sedentary"),
    LIGHT("Light exercise (1-2 days/week)", "light"),
    MODERATE("Moderate exercise (3-5 days/week)", "moderate"),
    HEAVY("Heavy exercise (6-7 days/week)",  "heavy"),
    ATHLETE("Athlete (2x per day)",  "athlete");

    private final String displayName;
    private final String code;

    ActivityLevel(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public String getCode() {
        return code;
    }
}
