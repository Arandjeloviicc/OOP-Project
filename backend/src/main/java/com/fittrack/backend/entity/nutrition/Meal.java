package com.fittrack.backend.entity.nutrition;

import com.fittrack.backend.entity.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "meals",
        indexes = {
                @Index(
                        name = "idx_meals_user_date_kind_id",
                        columnList = "user_id, meal_date, kind, id"
                ),
                @Index(
                        name = "idx_meals_user_kind_name",
                        columnList = "user_id, kind, name"
                )
        }
)
public class Meal {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Foreign key
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(name = "meal_date")
    private LocalDate mealDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealKind kind;

    // Created / Updated
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "meal")
    @OrderBy("id ASC")
    private List<MealItem> items = new ArrayList<>();

    // Constructor
    protected Meal() {}

    public Meal(User user, String name, LocalDate mealDate, MealKind kind) {
        this.user = user;
        this.name = name;
        this.mealDate = mealDate;
        this.kind = kind;
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

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public LocalDate getMealDate() {
        return mealDate;
    }

    public MealKind getKind() {
        return kind;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<MealItem> getItems() {
        return items;
    }

    // Setters

    public void setName(String name) {
        this.name = name;
    }
}