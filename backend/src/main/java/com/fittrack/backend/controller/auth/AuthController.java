package com.fittrack.backend.controller.auth;

import com.fittrack.backend.dto.auth.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.fittrack.backend.service.auth.LoginResult;
import com.fittrack.backend.service.auth.LoginService;
import org.jspecify.annotations.NonNull;
import com.fittrack.backend.entity.user.User;
import com.fittrack.backend.service.auth.RegistrationResult;
import com.fittrack.backend.service.auth.RegistrationService;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginService loginService;
    private final RegistrationService registrationService;

    public AuthController(LoginService loginService, RegistrationService registrationService) {
        this.loginService = loginService;
        this.registrationService = registrationService;
    }

    @PostMapping("/login")
    public ResponseEntity<@NonNull LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginService.login(
                request.email(),
                request.password()
        );

        return switch (result.status()) {
            case SUCCESS -> {
                UserResponse userResponse = new UserResponse(
                        result.userId(),
                        result.username(),
                        result.email()
                );

                yield ResponseEntity.ok(
                        new LoginResponse(
                                "SUCCESS",
                                userResponse,
                                result.profileSetupComplete()
                        )
                );
            }

            case USER_NOT_FOUND -> ResponseEntity.ok(
                    new LoginResponse(
                            "USER_NOT_FOUND",
                            null,
                            false
                    )
            );

            case WRONG_PASSWORD -> ResponseEntity.ok(
                    new LoginResponse(
                            "WRONG_PASSWORD",
                            null,
                            false
                    )
            );
        };
    }

    @PostMapping("/register")
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