package com.fittrack.backend.service.profile;

import com.fittrack.backend.dto.nutrition.goal.NutritionTargets;
import com.fittrack.backend.dto.profile.ProfileSetupRequest;
import com.fittrack.backend.repository.profile.ProfileSetupJdbcRepository;
import com.fittrack.backend.service.calculation.NutritionGoalCalculationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Set;

@Service
public class ProfileSetupService {

    private static final int MIN_AGE = 13;
    private static final int MAX_AGE = 120;

    private static final Set<Double> ALLOWED_WEEKLY_GOALS = Set.of(0.25, 0.5, 0.75, 1.0);

    private final ProfileSetupJdbcRepository profileSetupJdbcRepository;
    private final NutritionGoalCalculationService nutritionGoalCalculationService;

    public ProfileSetupService(ProfileSetupJdbcRepository profileSetupJdbcRepository, NutritionGoalCalculationService nutritionGoalCalculationService) {
        this.profileSetupJdbcRepository = profileSetupJdbcRepository;
        this.nutritionGoalCalculationService = nutritionGoalCalculationService;
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

    // ── Validation Helpers ─────────────────────────────────────
    private int validateProfileSetup(ProfileSetupRequest request) {
        int age = Period.between(
                request.dateOfBirth(),
                LocalDate.now()
        ).getYears();

        if (age < MIN_AGE || age > MAX_AGE) {
            throw new IllegalArgumentException("Age must be between 13 and 120.");
        }

        switch (request.goalType()) {
            case LOSE_WEIGHT -> {
                validateWeeklyGoal(request.weeklyGoal());

                if (request.goalWeight() != null && request.goalWeight() >= request.weight()) {
                    throw new IllegalArgumentException("Goal weight must be lower than current weight.");
                }
            }

            case GAIN_WEIGHT -> {
                validateWeeklyGoal(request.weeklyGoal());

                if (request.goalWeight() != null && request.goalWeight() <= request.weight()) {
                    throw new IllegalArgumentException("Goal weight must be higher than current weight.");
                }
            }

            case MAINTAIN_WEIGHT -> {
                if (request.goalWeight() != null || request.weeklyGoal() != null) {
                    throw new IllegalArgumentException("Maintain weight goal must not have goal weight or weekly goal.");
                }
            }
        }

        return age;
    }

    private void validateWeeklyGoal(Double weeklyGoal) {
        if (weeklyGoal == null || !ALLOWED_WEEKLY_GOALS.contains(weeklyGoal)) {
            throw new IllegalArgumentException("Weekly goal must be 0.25, 0.5, 0.75 or 1.0.");
        }
    }
}