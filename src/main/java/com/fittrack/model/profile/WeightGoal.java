package com.fittrack.model.profile;

public enum WeightGoal {
    LOSE_WEIGHT("Lose weight", "lose_weight"),
    MAINTAIN_WEIGHT("Maintain weight", "maintain_weight"),
    GAIN_WEIGHT("Gain weight", "gain_weight"),;

    private final String displayName;
    private final String code;

    WeightGoal(String displayName, String code) {
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
