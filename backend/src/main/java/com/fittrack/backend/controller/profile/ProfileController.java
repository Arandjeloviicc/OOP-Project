package com.fittrack.backend.controller.profile;

import com.fittrack.backend.dto.profile.ProfileResponse;
import com.fittrack.backend.service.profile.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}