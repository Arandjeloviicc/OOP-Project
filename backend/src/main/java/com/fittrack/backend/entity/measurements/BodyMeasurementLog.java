package com.fittrack.backend.entity.measurements;

import com.fittrack.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Getter
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
    private Instant loggedAt;

    // Created / Updated
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

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
        Instant now = Instant.now();

        if (loggedAt == null) {
            loggedAt = now;
        }

        createdAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }
}