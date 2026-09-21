package com.fittrack.backend.service.profile;

import com.fittrack.backend.dto.nutrition.goal.NutritionTargets;
import com.fittrack.backend.dto.profile.ProfileSetupRequest;
import com.fittrack.backend.repository.profile.ProfileSetupJdbcRepository;
import com.fittrack.backend.service.calculation.NutritionGoalCalculationService;
import com.fittrack.backend.service.nutrition.NutritionGoalValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
public class ProfileSetupService {

    private final Clock clock;

    private final ProfileSetupJdbcRepository profileSetupJdbcRepository;
    private final NutritionGoalCalculationService nutritionGoalCalculationService;
    private final NutritionGoalValidationService nutritionGoalValidationService;
    private final ProfileValidationService profileValidationService;

    public ProfileSetupService(Clock clock, ProfileSetupJdbcRepository profileSetupJdbcRepository, NutritionGoalCalculationService nutritionGoalCalculationService, NutritionGoalValidationService nutritionGoalValidationService, ProfileValidationService profileValidationService) {
        this.clock = clock;
        this.profileSetupJdbcRepository = profileSetupJdbcRepository;
        this.nutritionGoalCalculationService = nutritionGoalCalculationService;
        this.nutritionGoalValidationService = nutritionGoalValidationService;
        this.profileValidationService = profileValidationService;
    }

    @Transactional
    public void completeProfile(ProfileSetupRequest request) {
        LocalDate today = LocalDate.now(clock);

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

        profileSetupJdbcRepository.completeProfile(request, targets, today);
    }

    private int validateProfileSetup(ProfileSetupRequest request) {
        LocalDate today = LocalDate.now(clock);

        int age = profileValidationService.validateAge(request.dateOfBirth(), today);

        nutritionGoalValidationService.validate(
                request.goalType(),
                request.goalWeight(),
                request.weeklyGoal(),
                request.weight()
        );

        return age;
    }
}