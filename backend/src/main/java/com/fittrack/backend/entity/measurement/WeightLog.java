package com.fittrack.backend.entity.measurement;

import com.fittrack.backend.entity.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "weight_logs")
public class WeightLog {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Foreign key
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    @Column(nullable = false)
    private double weight;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor
    protected WeightLog() {}

    public WeightLog(User user, LocalDateTime loggedAt, double weight) {
        this.user = user;
        this.loggedAt = loggedAt;
        this.weight = weight;
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

    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }

    public double getWeight() {
        return weight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}