package com.fittrack.model;

public enum WeightGoal {
    LOSE_WEIGHT("Lose weight"),
    MAINTAIN_WEIGHT("Maintain weight"),
    GAIN_WEIGHT("Gain weight");

    private final String displayName;

    WeightGoal(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
