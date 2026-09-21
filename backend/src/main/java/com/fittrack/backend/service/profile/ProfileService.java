package com.fittrack.backend.service.profile;

import com.fittrack.backend.dto.profile.ProfileResponse;
import com.fittrack.backend.dto.profile.editor.PersonalInfoUpdateRequest;
import com.fittrack.backend.exception.ResourceNotFoundException;
import com.fittrack.backend.repository.profile.ProfileJdbcRepository;
import com.fittrack.backend.repository.profile.projection.PersonalInfoData;
import com.fittrack.backend.repository.profile.projection.ProfileData;
import com.fittrack.backend.service.nutrition.NutritionGoalService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

@Service
public class ProfileService {

    private final Clock clock;

    private final ProfileJdbcRepository profileJdbcRepository;
    private final NutritionGoalService nutritionGoalService;
    private final ProfileValidationService profileValidationService;

    public ProfileService(Clock clock, ProfileJdbcRepository profileJdbcRepository, NutritionGoalService nutritionGoalService, ProfileValidationService profileValidationService) {
        this.clock = clock;
        this.profileJdbcRepository = profileJdbcRepository;
        this.nutritionGoalService = nutritionGoalService;
        this.profileValidationService = profileValidationService;
    }

    public ProfileResponse getProfile(Integer userId) {
        ProfileData profile = profileJdbcRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found."));

        return toResponse(profile);
    }

    @Transactional
    public void updatePersonalInfo(Integer userId, PersonalInfoUpdateRequest request) {
        LocalDate today = LocalDate.now(clock);

        profileValidationService.validateAge(request.dateOfBirth(), today);

        PersonalInfoData current =
                profileJdbcRepository
                        .findPersonalInfoByUserId(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Profile not found.")
                        );

        boolean personalInfoChanged = hasPersonalInfoChanged(current, request);

        if (!personalInfoChanged) {
            return;
        }

        boolean nutritionInputsChanged = hasNutritionInputsChanged(current, request);

        int updated = profileJdbcRepository.updatePersonalInfo(userId, request);

        if (updated != 1) {
            throw new IllegalStateException("Failed to update personal information.");
        }

        if (nutritionInputsChanged) {
            nutritionGoalService.recalculateTargets(userId);
        }
    }

    private boolean hasPersonalInfoChanged(PersonalInfoData current, PersonalInfoUpdateRequest request) {
        return !current.firstName().equals(request.firstName().trim())
                || !current.lastName().equals(request.lastName().trim())
                || !current.dateOfBirth().equals(request.dateOfBirth())
                || current.gender() != request.gender()
                || Double.compare(
                current.height(),
                request.height()
        ) != 0;
    }

    private boolean hasNutritionInputsChanged(PersonalInfoData current, PersonalInfoUpdateRequest request) {
        return !current.dateOfBirth().equals(request.dateOfBirth())
                || current.gender() != request.gender()
                || Double.compare(
                current.height(),
                request.height()
        ) != 0;
    }

    private ProfileResponse toResponse(ProfileData profile) {
        return new ProfileResponse(
                profile.username(),
                profile.email(),

                profile.firstName(),
                profile.lastName(),
                profile.dateOfBirth(),
                profile.gender(),
                profile.height(),

                profile.currentWeight(),
                profile.startWeight(),

                profile.goalType(),
                profile.goalWeight(),
                profile.weeklyGoal(),
                profile.activityLevel(),

                profile.targetCalories(),
                profile.targetCarbs(),
                profile.targetFat(),
                profile.targetProtein()
        );
    }
}