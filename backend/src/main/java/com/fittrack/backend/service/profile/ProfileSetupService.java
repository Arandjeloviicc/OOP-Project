package com.fittrack.backend.service.profile;
import com.fittrack.backend.entity.measurement.WeightLog;
import com.fittrack.backend.entity.profile.UserProfile;
import com.fittrack.backend.repository.UserProfileRepository;
import com.fittrack.backend.repository.WeightLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileSetupService {

    private final UserProfileRepository userProfileRepository;
    private final WeightLogRepository weightLogRepository;

    public ProfileSetupService(UserProfileRepository userProfileRepository, WeightLogRepository weightLogRepository) {
        this.userProfileRepository = userProfileRepository;
        this.weightLogRepository = weightLogRepository;
    }

    @Transactional
    public void completeProfile(UserProfile userProfile, WeightLog weightLog) {
        userProfileRepository.save(userProfile);
        weightLogRepository.save(weightLog);
    }

    public boolean isProfileSetupComplete(Integer userId) {
        return userProfileRepository.existsByUserId(userId);
    }
}