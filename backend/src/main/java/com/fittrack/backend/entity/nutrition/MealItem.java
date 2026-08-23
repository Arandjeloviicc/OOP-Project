package com.fittrack.backend.entity.nutrition;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "meal_items",
        indexes = {
                @Index(
                        name = "idx_meal_items_meal_id_id",
                        columnList = "meal_id, id"
                )
        }
)
public class MealItem {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Foreign key
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Meal meal;

    // Foreign key
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Food food;

    @Column(name = "quantity_grams", nullable = false)
    private double quantityGrams;

    // Snapshot
    @Column(name = "food_name", nullable = false)
    private String foodName;

    @Column(name = "brand")
    private String brand;

    @Column(name = "serving_size_grams", nullable = false)
    private double servingSizeGrams;

    @Column(name = "calories_per_serving", nullable = false)
    private double caloriesPerServing;

    @Column(name = "protein_per_serving", nullable = false)
    private double proteinPerServing;

    @Column(name = "carbs_per_serving", nullable = false)
    private double carbsPerServing;

    @Column(name = "fat_per_serving", nullable = false)
    private double fatPerServing;

    // Created / Updated
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor
    protected MealItem() {}

    public MealItem(Meal meal, Food food, double quantityGrams) {
        this.meal = meal;
        this.food = food;
        this.quantityGrams = quantityGrams;

        this.foodName = food.getName();
        this.brand = food.getBrand();
        this.servingSizeGrams = food.getServingSizeGrams();
        this.caloriesPerServing = food.getCaloriesPerServing();
        this.proteinPerServing = food.getProteinPerServing();
        this.carbsPerServing = food.getCarbsPerServing();
        this.fatPerServing = food.getFatPerServing();
    }

    public MealItem(Meal meal, MealItem sourceItem) {
        this.meal = meal;
        this.food = sourceItem.getFood();
        this.quantityGrams = sourceItem.getQuantityGrams();

        this.foodName = sourceItem.getFoodName();
        this.brand = sourceItem.getBrand();
        this.servingSizeGrams = sourceItem.getServingSizeGrams();
        this.caloriesPerServing = sourceItem.getCaloriesPerServing();
        this.proteinPerServing = sourceItem.getProteinPerServing();
        this.carbsPerServing = sourceItem.getCarbsPerServing();
        this.fatPerServing = sourceItem.getFatPerServing();
    }

    // Default
    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public Meal getMeal() {
        return meal;
    }

    public Food getFood() {
        return food;
    }

    public double getQuantityGrams() {
        return quantityGrams;
    }

    public String getFoodName() {
        return foodName;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setQuantityGrams(double quantityGrams) {
        this.quantityGrams = quantityGrams;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }
}