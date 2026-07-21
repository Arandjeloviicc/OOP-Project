package com.fittrack.model.profile;

public enum Gender {
    MALE("male"),
    FEMALE("female");

    private final String code;

    Gender(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return switch (this) {
            case MALE -> "Male";
            case FEMALE -> "Female";
        };
    }
}
