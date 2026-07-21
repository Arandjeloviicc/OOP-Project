package com.fittrack.model.profile;

public enum WeightGoal {
    LOSE_WEIGHT("lose_weight"),
    MAINTAIN_WEIGHT("maintain_weight"),
    GAIN_WEIGHT("gain_weight"),;

    private final String code;

    WeightGoal(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return switch (this) {
            case LOSE_WEIGHT -> "Lose weight";
            case MAINTAIN_WEIGHT -> "Maintain weight";
            case GAIN_WEIGHT -> "Gain weight";
        };
    }
}
