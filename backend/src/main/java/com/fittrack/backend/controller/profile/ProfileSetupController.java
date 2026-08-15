package com.fittrack.backend.controller.profile;

import com.fittrack.backend.dto.profile.ProfileSetupRequest;
import com.fittrack.backend.entity.measurement.WeightLog;
import com.fittrack.backend.entity.profile.UserProfile;
import com.fittrack.backend.entity.user.User;
import com.fittrack.backend.repository.user.UserRepository;
import com.fittrack.backend.service.profile.ProfileSetupService;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/profile")
public class ProfileSetupController {

    private final UserRepository userRepository;
    private final ProfileSetupService profileSetupService;

    public ProfileSetupController(UserRepository userRepository, ProfileSetupService profileSetupService) {
        this.userRepository = userRepository;
        this.profileSetupService = profileSetupService;
    }

    @PostMapping("/setup")
    public ResponseEntity<@NonNull Void> completeProfile(@RequestBody ProfileSetupRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserProfile userProfile = new UserProfile(
                user,
                request.firstName(),
                request.lastName(),
                request.dateOfBirth(),
                request.gender(),
                request.height(),
                request.activityLevel(),
                request.goalType(),
                request.goalWeight(),
                request.weeklyGoal()
        );

        WeightLog weightLog = new WeightLog(
                user,
                LocalDateTime.now(),
                request.weight()
        );

        profileSetupService.completeProfile(userProfile, weightLog);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/setup-complete")
    public ResponseEntity<@NonNull Boolean> isProfileSetupComplete(@PathVariable Integer userId) {
        boolean complete = profileSetupService.isProfileSetupComplete(userId);

        return ResponseEntity.ok(complete);
    }
}