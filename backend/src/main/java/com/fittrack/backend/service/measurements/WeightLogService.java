package com.fittrack.backend.service.measurements;

import com.fittrack.backend.dto.measurements.weight.WeightLogRequest;
import com.fittrack.backend.dto.measurements.weight.WeightLogResponse;
import com.fittrack.backend.exception.ConflictException;
import com.fittrack.backend.exception.ResourceNotFoundException;
import com.fittrack.backend.repository.measurements.weight.WeightLogJdbcRepository;
import com.fittrack.backend.repository.measurements.weight.projection.DeleteWeightLogResult;
import com.fittrack.backend.repository.measurements.weight.projection.UpdateWeightLogResult;
import com.fittrack.backend.service.nutrition.NutritionGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeightLogService {

    private final Clock clock;

    private final WeightLogJdbcRepository weightLogJdbcRepository;
    private final NutritionGoalService nutritionGoalService;

    public List<WeightLogResponse> getWeightHistory(Integer userId) {
        return weightLogJdbcRepository.findAllByUserId(userId);
    }

    @Transactional
    public WeightLogResponse createWeightLog(Integer userId, WeightLogRequest request) {
        Instant loggedAt = Instant.now(clock);

        WeightLogResponse created =
                weightLogJdbcRepository
                        .create(userId, request, loggedAt)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("User not found.")
                        );

        nutritionGoalService.recalculateTargets(userId);

        return created;
    }

    @Transactional
    public WeightLogResponse updateWeightLog(Integer userId, Integer weightLogId, WeightLogRequest request) {
        UpdateWeightLogResult result =
                weightLogJdbcRepository
                        .update(weightLogId, userId, request)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Weight log not found.")
                        );

        if (result.wasLatest()) {
            nutritionGoalService.recalculateTargets(userId);
        }

        return result.weightLog();
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