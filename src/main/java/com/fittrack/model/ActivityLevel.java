package com.fittrack.model;

public enum ActivityLevel {
    SEDENTARY("Sedentary (office job)"),
    LIGHT("Light exercise (1-2 days/week)"),
    MODERATE("Moderate exercise (3-5 days/week)"),
    HEAVY("Heavy exercise (6-7 days/week)"),
    ATHLETE("Athlete (2x per day)");

    private final String displayName;

    ActivityLevel(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
