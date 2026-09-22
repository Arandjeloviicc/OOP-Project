package com.fittrack.backend.service.measurements;

import com.fittrack.backend.dto.measurements.body.BodyMeasurementRequest;
import com.fittrack.backend.dto.measurements.body.BodyMeasurementResponse;
import com.fittrack.backend.exception.ResourceNotFoundException;
import com.fittrack.backend.repository.measurements.body.BodyMeasurementJdbcRepository;
import com.fittrack.backend.repository.measurements.body.projection.DeleteBodyMeasurementResult;
import com.fittrack.backend.repository.measurements.body.projection.UpdateBodyMeasurementResult;
import com.fittrack.backend.repository.profile.ProfileJdbcRepository;
import com.fittrack.backend.repository.profile.projection.PersonalInfoData;
import com.fittrack.backend.service.calculation.BodyFatCalculationService;
import com.fittrack.backend.service.nutrition.NutritionGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BodyMeasurementService {

    private final Clock clock;

    private final BodyMeasurementJdbcRepository bodyMeasurementJdbcRepository;
    private final ProfileJdbcRepository profileJdbcRepository;

    private final BodyFatCalculationService bodyFatCalculationService;
    private final NutritionGoalService nutritionGoalService;

    public List<BodyMeasurementResponse> getBodyMeasurementHistory(Integer userId) {
        return bodyMeasurementJdbcRepository.findAllByUserId(userId);
    }

    @Transactional
    public BodyMeasurementResponse createBodyMeasurement(Integer userId, BodyMeasurementRequest request) {
        PersonalInfoData personalInfo =
                profileJdbcRepository
                        .findPersonalInfoByUserId(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("User profile not found.")
                        );

        bodyFatCalculationService.validate(
                personalInfo.gender(),
                personalInfo.height(),
                request.neck(),
                request.waist(),
                request.hip()
        );

        Instant loggedAt = Instant.now(clock);

        BodyMeasurementResponse created =
                bodyMeasurementJdbcRepository
                        .create(userId, request, loggedAt)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("User not found.")
                        );

        nutritionGoalService.recalculateTargets(userId);

        return created;
    }

    @Transactional
    public BodyMeasurementResponse updateBodyMeasurement(Integer userId, Integer measurementId, BodyMeasurementRequest request) {
        PersonalInfoData personalInfo =
                profileJdbcRepository
                        .findPersonalInfoByUserId(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("User profile not found.")
                        );

        bodyFatCalculationService.validate(
                personalInfo.gender(),
                personalInfo.height(),
                request.neck(),
                request.waist(),
                request.hip()
        );

        UpdateBodyMeasurementResult result =
                bodyMeasurementJdbcRepository
                        .update(measurementId, userId, request)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Body measurement not found.")
                        );

        if (result.wasLatest()) {
            nutritionGoalService.recalculateTargets(userId);
        }

        return result.measurement();
    }

    @Transactional
    public void deleteBodyMeasurement(Integer userId, Integer measurementId) {
        DeleteBodyMeasurementResult result =
                bodyMeasurementJdbcRepository.delete(
                        measurementId,
                        userId
                );

        if (!result.deleted()) {
            throw new ResourceNotFoundException("Body measurement not found.");
        }

        if (result.wasLatest()) {
            nutritionGoalService.recalculateTargets(userId);
        }
    }
}