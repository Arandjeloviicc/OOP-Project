package com.fittrack.backend.entity.nutrition;

import com.fittrack.backend.entity.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(
        name = "foods",
        indexes = {
                @Index(
                        name = "idx_foods_name",
                        columnList = "name"
                ),
                @Index(
                        name = "idx_foods_created_by_user_name",
                        columnList = "created_by_user_id, name"
                )
        }
)
public class Food {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column()
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User createdByUser;

    // Created / Updated
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Constructor
    protected Food() {}

    public Food(String name, String brand, double servingSizeGrams, double caloriesPerServing, double proteinPerServing, double carbsPerServing, double fatPerServing, User createdByUser) {
        this.name = name;
        this.brand = brand;
        this.servingSizeGrams = servingSizeGrams;
        this.caloriesPerServing = caloriesPerServing;
        this.proteinPerServing = proteinPerServing;
        this.carbsPerServing = carbsPerServing;
        this.fatPerServing = fatPerServing;
        this.createdByUser = createdByUser;
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

    // Getters
    public Integer getId() {
        return id;
    }

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

    public User getCreatedByUser() {
        return createdByUser;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}