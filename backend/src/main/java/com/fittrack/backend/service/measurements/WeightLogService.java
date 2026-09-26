package com.fittrack.backend.service.measurements;

import com.fittrack.backend.dto.measurements.weight.WeightHistoryResponse;
import com.fittrack.backend.dto.measurements.weight.WeightLogRequest;
import com.fittrack.backend.dto.measurements.weight.WeightLogResponse;
import com.fittrack.backend.exception.ConflictException;
import com.fittrack.backend.exception.ResourceNotFoundException;
import com.fittrack.backend.repository.measurements.weight.WeightLogJdbcRepository;
import com.fittrack.backend.repository.measurements.weight.projection.CreateWeightLogResult;
import com.fittrack.backend.repository.measurements.weight.projection.DeleteWeightLogResult;
import com.fittrack.backend.repository.measurements.weight.projection.UpdateWeightLogResult;
import com.fittrack.backend.service.nutrition.NutritionGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WeightLogService {

    private final WeightLogJdbcRepository weightLogJdbcRepository;
    private final NutritionGoalService nutritionGoalService;

    public WeightHistoryResponse getWeightHistory(Integer userId) {
        return weightLogJdbcRepository.findHistoryByUserId(userId);
    }

    @Transactional
    public WeightLogResponse createWeightLog(Integer userId, WeightLogRequest request) {
        CreateWeightLogResult result = weightLogJdbcRepository.create(userId, request);

        switch (result.status()) {
            case USER_NOT_FOUND -> throw new ResourceNotFoundException("User not found.");

            case DATE_CONFLICT -> throw new ConflictException("A weight log already exists for this date.");

            case CREATED -> {
                if (result.isLatest()) {
                    nutritionGoalService.recalculateTargets(userId);
                }

                return result.weightLog();
            }
        }

        throw new IllegalStateException("Unexpected weight log creation status.");
    }

    @Transactional
    public WeightLogResponse updateWeightLog(Integer userId, Integer weightLogId, WeightLogRequest request) {
        UpdateWeightLogResult result = weightLogJdbcRepository.update(weightLogId, userId, request);

        switch (result.status()) {
            case NOT_FOUND -> throw new ResourceNotFoundException("Weight log not found.");

            case DATE_CONFLICT -> throw new ConflictException("A weight log already exists for this date.");

            case UPDATED -> {
                if (result.wasLatest() || result.isLatest()) {
                    nutritionGoalService.recalculateTargets(userId);
                }

                return result.weightLog();
            }
        }

        throw new IllegalStateException("Unexpected weight log update status.");
    }

    @Transactional
    public void deleteWeightLog(Integer userId, Integer weightLogId) {
        DeleteWeightLogResult result = weightLogJdbcRepository.delete(weightLogId, userId);

        switch (result.status()) {
            case NOT_FOUND ->
                    throw new ResourceNotFoundException("Weight log not found.");

            case LAST_REMAINING ->
                    throw new ConflictException("At least one weight log must be kept.");

            case DELETED -> {
                if (result.wasLatest()) {
                    nutritionGoalService.recalculateTargets(userId);
                }
            }
        }
    }
}