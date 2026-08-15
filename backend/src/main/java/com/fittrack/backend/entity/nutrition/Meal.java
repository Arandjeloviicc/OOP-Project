package com.fittrack.backend.entity.nutrition;

import com.fittrack.backend.entity.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "meals")
public class Meal {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Foreign key
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    // Created / Updated
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor
    protected Meal() {}

    public Meal(User user, String name, LocalDate mealDate) {
        this.user = user;
        this.name = name;
        this.mealDate = mealDate;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}