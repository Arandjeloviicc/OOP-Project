package com.fittrack.backend.service.nutrition;

import com.fittrack.backend.dto.nutrition.goal.NutritionTargets;
import com.fittrack.backend.dto.profile.editor.NutritionGoalUpdateRequest;
import com.fittrack.backend.exception.ResourceNotFoundException;
import com.fittrack.backend.repository.nutrition.goal.NutritionGoalJdbcRepository;
import com.fittrack.backend.repository.nutrition.goal.NutritionGoalRecalculationData;
import com.fittrack.backend.service.calculation.NutritionGoalCalculationService;
import com.fittrack.backend.service.measurement.BodyFatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

@Service
public class NutritionGoalService {

    private final NutritionGoalJdbcRepository nutritionGoalJdbcRepository;
    private final NutritionGoalCalculationService nutritionGoalCalculationService;
    private final NutritionGoalValidationService nutritionGoalValidationService;
    private final BodyFatService bodyFatService;

    public NutritionGoalService(NutritionGoalJdbcRepository nutritionGoalJdbcRepository, NutritionGoalCalculationService nutritionGoalCalculationService, NutritionGoalValidationService nutritionGoalValidationService, BodyFatService bodyFatService) {
        this.nutritionGoalJdbcRepository = nutritionGoalJdbcRepository;
        this.nutritionGoalCalculationService = nutritionGoalCalculationService;
        this.nutritionGoalValidationService = nutritionGoalValidationService;
        this.bodyFatService = bodyFatService;
    }

    public NutritionTargets getTargetsForDate(Integer userId, LocalDate date) {
        return nutritionGoalJdbcRepository
                .findTargetsForDate(userId, date)
                .orElseThrow(() ->
                        new IllegalArgumentException("Nutrition goal not found for selected date.")
                );
    }

    @Transactional
    public void updateGoal(Integer userId, NutritionGoalUpdateRequest request) {
        NutritionGoalRecalculationData data =
                nutritionGoalJdbcRepository
                        .findRecalculationData(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Nutrition goal not found.")
                        );

        if (data.currentWeight() == null) {
            throw new IllegalStateException("Current weight is required to calculate nutrition goals.");
        }

        nutritionGoalValidationService.validate(
                request.goalType(),
                request.goalWeight(),
                request.weeklyGoal(),
                data.currentWeight()
        );

        if (!hasGoalChanged(data, request)) {
            return;
        }

        LocalDate today = LocalDate.now();

        if (data.startDate().isAfter(today)) {
            throw new IllegalStateException("Active nutrition goal cannot start in the future.");
        }

        int age = Period.between(data.dateOfBirth(), today).getYears();

        Double bodyFat =
                bodyFatService.calculateLatestBodyFat(
                        userId,
                        data.gender(),
                        data.height()
                );

        NutritionTargets targets =
                nutritionGoalCalculationService.calculate(
                        age,
                        data.gender(),
                        data.height(),
                        data.currentWeight(),
                        bodyFat,

                        request.activityLevel(),
                        request.goalType(),
                        request.weeklyGoal()
                );

        if (data.startDate().isEqual(today)) {
            int updated = nutritionGoalJdbcRepository.updateGoal(
                    data.goalId(),
                    request,
                    targets
            );

            if (updated != 1) {
                throw new IllegalStateException("Failed to update nutrition goal.");
            }

            return;
        }

        int closed = nutritionGoalJdbcRepository.closeGoal(
                data.goalId(),
                today.minusDays(1)
        );

        if (closed != 1) {
            throw new IllegalStateException("Failed to close active nutrition goal.");
        }

        int inserted = nutritionGoalJdbcRepository.insertGoalVersion(
                userId,
                request,
                targets,
                today
        );

        if (inserted != 1) {
            throw new IllegalStateException("Failed to create nutrition goal.");
        }
    }

    private boolean hasGoalChanged(NutritionGoalRecalculationData current, NutritionGoalUpdateRequest request) {
        return current.activityLevel() != request.activityLevel()
                || current.goalType() != request.goalType()
                || !Objects.equals(
                current.goalWeight(),
                request.goalWeight()
        )
                || !Objects.equals(
                current.weeklyGoal(),
                request.weeklyGoal()
        );
    }

    @Transactional
    public void recalculateTargets(Integer userId) {
        NutritionGoalRecalculationData data =
                nutritionGoalJdbcRepository
                        .findRecalculationData(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Nutrition goal not found.")
                        );

        if (data.currentWeight() == null) {
            throw new IllegalStateException("Current weight is required to calculate nutrition goals.");
        }

        LocalDate today = LocalDate.now();

        if (data.startDate().isAfter(today)) {
            throw new IllegalStateException("Active nutrition goal cannot start in the future.");
        }

        int age = Period.between(data.dateOfBirth(), today).getYears();

        Double bodyFat =
                bodyFatService.calculateLatestBodyFat(
                        userId,
                        data.gender(),
                        data.height()
                );

        NutritionTargets targets =
                nutritionGoalCalculationService.calculate(
                        age,
                        data.gender(),
                        data.height(),
                        data.currentWeight(),
                        bodyFat,
                        data.activityLevel(),
                        data.goalType(),
                        data.weeklyGoal()
                );

        if (data.startDate().isEqual(today)) {
            int updated = nutritionGoalJdbcRepository.updateTargets(
                    data.goalId(),
                    targets
            );

            if (updated != 1) {
                throw new IllegalStateException("Failed to update nutrition goal.");
            }

            return;
        }

        int closed = nutritionGoalJdbcRepository.closeGoal(
                    data.goalId(),
                    today.minusDays(1)
        );

        if (closed != 1) {
            throw new IllegalStateException(
                    "Failed to close active nutrition goal."
            );
        }

        int inserted = nutritionGoalJdbcRepository.insertGoalVersion(
                    userId,
                    data,
                    targets,
                    today
        );

        if (inserted != 1) {
            throw new IllegalStateException(
                    "Failed to create nutrition goal."
            );
        }
    }
}