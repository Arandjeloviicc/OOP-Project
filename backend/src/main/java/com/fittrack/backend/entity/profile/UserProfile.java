package com.fittrack.backend.entity.profile;

import com.fittrack.backend.entity.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    // Primary key
    @Id
    @Column(name = "user_id")
    private Integer userId;

    // Foreign key
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    // Enum
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private double height;

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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructor
    protected UserProfile() {}

    public UserProfile(
            User user,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            Gender gender,
            double height,
            ActivityLevel activityLevel,
            WeightGoal goalType,
            Double goalWeight,
            Double weeklyGoal
    ) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.height = height;
        this.activityLevel = activityLevel;
        this.goalType = goalType;
        this.goalWeight = goalWeight;
        this.weeklyGoal = weeklyGoal;
    }

    // Default
    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }

    // Getters
    public Integer getUserId() {
        return userId;
    }

    public User getUser() {
        return user;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public double getHeight() {
        return height;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}