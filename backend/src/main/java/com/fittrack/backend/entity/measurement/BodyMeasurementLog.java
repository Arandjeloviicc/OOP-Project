package com.fittrack.backend.entity.measurement;

import com.fittrack.backend.entity.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "body_measurement_logs")
public class BodyMeasurementLog {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Foreign key
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private double waist;

    @Column(nullable = false)
    private double neck;

    @Column
    private Double hip;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    // Created / Updated
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor
    protected BodyMeasurementLog() {}

    public BodyMeasurementLog(User user, double waist, double neck, Double hip) {
        this.user = user;
        this.waist = waist;
        this.neck = neck;
        this.hip = hip;
    }

    // Default
    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (loggedAt == null) {
            loggedAt = now;
        }

        createdAt = now;
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

    public Double getWaist() {
        return waist;
    }

    public Double getNeck() {
        return neck;
    }

    public Double getHip() {
        return hip;
    }

    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}