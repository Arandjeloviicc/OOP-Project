package com.fittrack.backend.controller.profile;

import com.fittrack.backend.dto.profile.ProfileResponse;
import com.fittrack.backend.dto.profile.editor.PersonalInfoUpdateRequest;
import com.fittrack.backend.service.profile.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/user/{userId}")
    public ProfileResponse getProfile(@PathVariable Integer userId) {
        return profileService.getProfile(userId);
    }

    @PutMapping("/user/{userId}/personal-info")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePersonalInfo(@PathVariable Integer userId, @RequestBody PersonalInfoUpdateRequest request) {
        profileService.updatePersonalInfo(userId, request);
    }
}