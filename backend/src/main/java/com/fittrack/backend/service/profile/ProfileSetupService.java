package com.fittrack.backend.service.profile;

import com.fittrack.backend.dto.nutrition.goal.NutritionTargets;
import com.fittrack.backend.dto.profile.ProfileSetupRequest;
import com.fittrack.backend.repository.profile.ProfileSetupJdbcRepository;
import com.fittrack.backend.service.calculation.NutritionGoalCalculationService;
import com.fittrack.backend.service.nutrition.NutritionGoalValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Service
public class ProfileSetupService {

    private static final int MIN_AGE = 13;
    private static final int MAX_AGE = 120;

    private final ProfileSetupJdbcRepository profileSetupJdbcRepository;
    private final NutritionGoalCalculationService nutritionGoalCalculationService;
    private final NutritionGoalValidationService nutritionGoalValidationService;

    public ProfileSetupService(ProfileSetupJdbcRepository profileSetupJdbcRepository, NutritionGoalCalculationService nutritionGoalCalculationService, NutritionGoalValidationService nutritionGoalValidationService) {
        this.profileSetupJdbcRepository = profileSetupJdbcRepository;
        this.nutritionGoalCalculationService = nutritionGoalCalculationService;
        this.nutritionGoalValidationService = nutritionGoalValidationService;
    }

    @Transactional
    public void completeProfile(ProfileSetupRequest request) {
        int age = validateProfileSetup(request);

        NutritionTargets targets =
                nutritionGoalCalculationService.calculate(
                        age,
                        request.gender(),
                        request.height(),
                        request.weight(),
                        null,
                        request.activityLevel(),
                        request.goalType(),
                        request.weeklyGoal()
                );

        profileSetupJdbcRepository.completeProfile(request, targets);
    }

    private int validateProfileSetup(ProfileSetupRequest request) {
        int age = Period.between(request.dateOfBirth(), LocalDate.now()).getYears();

        if (age < MIN_AGE || age > MAX_AGE) {
            throw new IllegalArgumentException("Age must be between 13 and 120.");
        }

        nutritionGoalValidationService.validate(
                request.goalType(),
                request.goalWeight(),
                request.weeklyGoal(),
                request.weight()
        );

        return age;
    }
}