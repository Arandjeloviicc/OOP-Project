package com.fittrack.backend.service.measurements;

import com.fittrack.backend.dto.measurements.body.BodyMeasurementHistoryResponse;
import com.fittrack.backend.dto.measurements.body.BodyMeasurementRequest;
import com.fittrack.backend.dto.measurements.body.BodyMeasurementResponse;
import com.fittrack.backend.exception.ConflictException;
import com.fittrack.backend.exception.ResourceNotFoundException;
import com.fittrack.backend.repository.measurements.body.BodyMeasurementJdbcRepository;
import com.fittrack.backend.repository.measurements.body.projection.CreateBodyMeasurementResult;
import com.fittrack.backend.repository.measurements.body.projection.DeleteBodyMeasurementResult;
import com.fittrack.backend.repository.measurements.body.projection.UpdateBodyMeasurementResult;
import com.fittrack.backend.repository.profile.ProfileJdbcRepository;
import com.fittrack.backend.repository.profile.projection.PersonalInfoData;
import com.fittrack.backend.service.calculation.BodyFatCalculationService;
import com.fittrack.backend.service.nutrition.NutritionGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BodyMeasurementService {

    private final BodyMeasurementJdbcRepository bodyMeasurementJdbcRepository;
    private final ProfileJdbcRepository profileJdbcRepository;

    private final BodyFatCalculationService bodyFatCalculationService;
    private final NutritionGoalService nutritionGoalService;

    public BodyMeasurementHistoryResponse getBodyMeasurementHistory(Integer userId) {
        return bodyMeasurementJdbcRepository.findHistoryByUserId(userId);
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

        CreateBodyMeasurementResult result = bodyMeasurementJdbcRepository.create(userId, request);

        switch (result.status()) {
            case USER_NOT_FOUND -> throw new ResourceNotFoundException("User not found.");

            case DATE_CONFLICT -> throw new ConflictException("A body measurement already exists for this date.");

            case CREATED -> {
                if (result.isLatest()) {
                    nutritionGoalService.recalculateTargets(userId);
                }

                return result.bodyMeasurement();
            }
        }

        throw new IllegalStateException("Unexpected body measurement creation status.");
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

        UpdateBodyMeasurementResult result = bodyMeasurementJdbcRepository.update(measurementId, userId, request);

        switch (result.status()) {
            case NOT_FOUND -> throw new ResourceNotFoundException("Body measurement not found.");

            case DATE_CONFLICT -> throw new ConflictException("A body measurement already exists for this date.");

            case UPDATED -> {
                if (result.wasLatest() || result.isLatest()) {
                    nutritionGoalService.recalculateTargets(userId);
                }

                return result.measurement();
            }
        }

        throw new IllegalStateException("Unexpected body measurement update status.");
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