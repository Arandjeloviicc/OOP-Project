package com.fittrack.backend.controller.auth;

import com.fittrack.backend.dto.auth.RegisterRequest;
import com.fittrack.backend.dto.auth.RegisterResponse;
import com.fittrack.backend.dto.auth.UserResponse;
import com.fittrack.backend.entity.user.User;
import com.fittrack.backend.service.auth.register.RegistrationResult;
import com.fittrack.backend.service.auth.register.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<@NonNull RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegistrationResult result = registrationService.register(
                request.username(),
                request.email(),
                request.password()
        );

        return switch (result.status()) {
            case SUCCESS -> {
                User user = result.user();

                UserResponse userResponse = new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail()
                );

                yield ResponseEntity.ok(
                        new RegisterResponse(
                                "SUCCESS",
                                userResponse
                        )
                );
            }

            case USERNAME_TAKEN -> ResponseEntity.ok(
                    new RegisterResponse(
                            "USERNAME_TAKEN",
                            null
                    )
            );

            case EMAIL_TAKEN -> ResponseEntity.ok(
                    new RegisterResponse(
                            "EMAIL_TAKEN",
                            null
                    )
            );
        };
    }
}