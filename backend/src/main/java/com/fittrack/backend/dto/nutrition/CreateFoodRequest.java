package com.fittrack.backend.dto.nutrition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public class CreateFoodRequest {

    @NotBlank
    private String name;

    private String brand;

    @Positive
    private double servingSizeGrams;

    @PositiveOrZero
    private double caloriesPerServing;

    @PositiveOrZero
    private double proteinPerServing;

    @PositiveOrZero
    private double carbsPerServing;

    @PositiveOrZero
    private double fatPerServing;

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public double getServingSizeGrams() {
        return servingSizeGrams;
    }

    public double getCaloriesPerServing() {
        return caloriesPerServing;
    }

    public double getProteinPerServing() {
        return proteinPerServing;
    }

    public double getCarbsPerServing() {
        return carbsPerServing;
    }

    public double getFatPerServing() {
        return fatPerServing;
    }
}
