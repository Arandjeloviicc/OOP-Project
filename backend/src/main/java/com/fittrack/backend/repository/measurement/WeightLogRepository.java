package com.fittrack.backend.repository.measurement;

import org.jspecify.annotations.NonNull;
import com.fittrack.backend.entity.measurement.WeightLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeightLogRepository extends JpaRepository<@NonNull WeightLog, @NonNull Integer> {
}