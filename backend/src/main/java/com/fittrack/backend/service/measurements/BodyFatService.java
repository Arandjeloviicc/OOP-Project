package com.fittrack.backend.service.measurements;

import com.fittrack.backend.entity.profile.Gender;
import com.fittrack.backend.repository.measurements.body.BodyMeasurementJdbcRepository;
import com.fittrack.backend.repository.measurements.body.projection.LatestBodyMeasurement;
import com.fittrack.backend.service.calculation.BodyFatCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BodyFatService {

    private final BodyMeasurementJdbcRepository bodyMeasurementJdbcRepository;
    private final BodyFatCalculationService bodyFatCalculationService;

    public Double calculateLatestBodyFat(Integer userId, Gender gender, double heightCm) {
        Optional<LatestBodyMeasurement> latestMeasurement = bodyMeasurementJdbcRepository.findLatestByUserId(userId);

        if (latestMeasurement.isEmpty()) {
            return null;
        }

        LatestBodyMeasurement measurement = latestMeasurement.get();

        if (measurement.neck() == null || measurement.waist() == null) {
            return null;
        }

        if (gender == Gender.FEMALE && measurement.hip() == null) {
            return null;
        }

        return bodyFatCalculationService.calculate(
                gender,
                heightCm,
                measurement.neck(),
                measurement.waist(),
                measurement.hip()
        );
    }
}
