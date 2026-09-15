package com.fittrack.backend.entity.nutrition;

import com.fittrack.backend.entity.profile.ActivityLevel;
import com.fittrack.backend.entity.profile.WeightGoal;
import com.fittrack.backend.entity.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "nutrition_goals",
        indexes = {
                @Index(
                        name = "idx_nutrition_goals_user_start_date",
                        columnList = "user_id, start_date"
                )
        }
)
public class NutritionGoal {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Foreign key
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    // Enum
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false)
    private ActivityLevel activityLevel;

    // Enum
    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false)
    private WeightGoal goalType;

    @Column(name = "goal_weight")
    private Double goalWeight;

    @Column(name = "weekly_goal")
    private Double weeklyGoal;

    @Column(name = "target_calories", nullable = false)
    private int targetCalories;

    @Column(name = "target_protein", nullable = false)
    private double targetProtein;

    @Column(name = "target_carbs", nullable = false)
    private double targetCarbs;

    @Column(name = "target_fat", nullable = false)
    private double targetFat;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // Created / Updated
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor
    protected NutritionGoal() {}

    public NutritionGoal(
            User user,
            ActivityLevel activityLevel,
            WeightGoal goalType,
            Double goalWeight,
            Double weeklyGoal,
            int targetCalories,
            double targetProtein,
            double targetCarbs,
            double targetFat,
            LocalDate startDate
    ) {
        this.user = user;
        this.activityLevel = activityLevel;
        this.goalType = goalType;
        this.goalWeight = goalWeight;
        this.weeklyGoal = weeklyGoal;
        this.targetCalories = targetCalories;
        this.targetProtein = targetProtein;
        this.targetCarbs = targetCarbs;
        this.targetFat = targetFat;
        this.startDate = startDate;
    }

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

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public WeightGoal getGoalType() {
        return goalType;
    }

    public Double getGoalWeight() {
        return goalWeight;
    }

    public Double getWeeklyGoal() {
        return weeklyGoal;
    }

    public int getTargetCalories() {
        return targetCalories;
    }

    public double getTargetProtein() {
        return targetProtein;
    }

    public double getTargetCarbs() {
        return targetCarbs;
    }

    public double getTargetFat() {
        return targetFat;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}