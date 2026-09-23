package com.fittrack.backend.entity.nutrition;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
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
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Meal meal;

    // Foreign key
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Food food;

    // Setters
    @Setter
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
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Constructor
    protected MealItem() {}

    public MealItem(Meal meal, Food food, String foodName, String brand, double quantityGrams, double servingSizeGrams, double caloriesPerServing, double proteinPerServing, double carbsPerServing, double fatPerServing) {
        this.meal = meal;
        this.food = food;
        this.foodName = foodName;
        this.brand = brand;
        this.quantityGrams = quantityGrams;
        this.servingSizeGrams = servingSizeGrams;
        this.caloriesPerServing = caloriesPerServing;
        this.proteinPerServing = proteinPerServing;
        this.carbsPerServing = carbsPerServing;
        this.fatPerServing = fatPerServing;
    }

    public MealItem(Meal meal, Food food, double quantityGrams) {
        this(meal, food, food.getName(), food.getBrand(), quantityGrams, food.getServingSizeGrams(), food.getCaloriesPerServing(), food.getProteinPerServing(), food.getCarbsPerServing(), food.getFatPerServing());
    }

    public MealItem(Meal meal, MealItem sourceItem) {
        this(meal, sourceItem.getFood(), sourceItem.getFoodName(), sourceItem.getBrand(), sourceItem.getQuantityGrams(), sourceItem.getServingSizeGrams(), sourceItem.getCaloriesPerServing(), sourceItem.getProteinPerServing(), sourceItem.getCarbsPerServing(), sourceItem.getFatPerServing());
    }

    // Default
    @PrePersist
    private void prePersist() {
        createdAt = Instant.now();
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }

}