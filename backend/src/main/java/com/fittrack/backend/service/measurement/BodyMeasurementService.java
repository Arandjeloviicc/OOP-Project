package com.fittrack.backend.service.measurement;

import com.fittrack.backend.entity.profile.Gender;
import com.fittrack.backend.repository.measurement.BodyMeasurementJdbcRepository;
import com.fittrack.backend.repository.measurement.projection.LatestBodyMeasurement;
import com.fittrack.backend.service.calculation.BodyFatCalculationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BodyMeasurementService {

    private final BodyMeasurementJdbcRepository bodyMeasurementJdbcRepository;
    private final BodyFatCalculationService bodyFatCalculationService;

    public BodyMeasurementService(BodyMeasurementJdbcRepository bodyMeasurementJdbcRepository, BodyFatCalculationService bodyFatCalculationService) {
        this.bodyMeasurementJdbcRepository = bodyMeasurementJdbcRepository;
        this.bodyFatCalculationService = bodyFatCalculationService;
    }

    public Double calculateLatestBodyFat(Integer userId, Gender gender, double heightCm) {
        Optional<LatestBodyMeasurement> latestMeasurement = bodyMeasurementJdbcRepository.findLatestByUserId(userId);

        if (latestMeasurement.isEmpty()) {
            return null;
        }

        LatestBodyMeasurement measurement = latestMeasurement.get();

        return bodyFatCalculationService.calculate(
                gender,
                heightCm,
                measurement.neck(),
                measurement.waist(),
                measurement.hip()
        );
    }
}
