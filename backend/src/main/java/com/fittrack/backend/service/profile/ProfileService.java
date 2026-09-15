package com.fittrack.backend.service.profile;

import com.fittrack.backend.dto.profile.ProfileResponse;
import com.fittrack.backend.repository.profile.ProfileJdbcRepository;
import com.fittrack.backend.repository.profile.projection.ProfileData;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final ProfileJdbcRepository profileJdbcRepository;

    public ProfileService(ProfileJdbcRepository profileJdbcRepository) {
        this.profileJdbcRepository = profileJdbcRepository;
    }

    public ProfileResponse getProfile(Integer userId) {
        ProfileData profile = profileJdbcRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found."));

        return toResponse(profile);
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
