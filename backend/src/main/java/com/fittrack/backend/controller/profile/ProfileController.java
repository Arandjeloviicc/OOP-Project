package com.fittrack.backend.controller.profile;

import com.fittrack.backend.dto.profile.ProfileResponse;
import com.fittrack.backend.dto.profile.editor.NutritionGoalUpdateRequest;
import com.fittrack.backend.dto.profile.editor.PersonalInfoUpdateRequest;
import com.fittrack.backend.service.nutrition.NutritionGoalService;
import com.fittrack.backend.service.profile.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final NutritionGoalService nutritionGoalService;

    @GetMapping("/user/{userId}")
    public ProfileResponse getProfile(@PathVariable Integer userId) {
        return profileService.getProfile(userId);
    }

    @PutMapping("/user/{userId}/personal-info")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePersonalInfo(@PathVariable Integer userId, @Valid @RequestBody PersonalInfoUpdateRequest request) {
        profileService.updatePersonalInfo(userId, request);
    }

    @PutMapping("/user/{userId}/nutrition-goal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateNutritionGoal(@PathVariable Integer userId, @Valid @RequestBody NutritionGoalUpdateRequest request) {
        nutritionGoalService.updateGoal(userId, request);
    }
}