package com.fittrack.backend.controller.profile;

import com.fittrack.backend.dto.profile.ProfileSetupRequest;
import com.fittrack.backend.service.profile.ProfileSetupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileSetupController {

    private final ProfileSetupService profileSetupService;

    @PostMapping("/setup")
    public ResponseEntity<@NonNull Void> completeProfile(@Valid @RequestBody ProfileSetupRequest request) {
        profileSetupService.completeProfile(request);

        return ResponseEntity.ok().build();
    }
}