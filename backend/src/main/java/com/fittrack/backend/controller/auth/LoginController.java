package com.fittrack.backend.controller.auth;

import com.fittrack.backend.dto.auth.LoginRequest;
import com.fittrack.backend.dto.auth.LoginResponse;
import com.fittrack.backend.dto.auth.UserResponse;
import com.fittrack.backend.service.auth.login.LoginResult;
import com.fittrack.backend.service.auth.login.LoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/login")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping
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
}