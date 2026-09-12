package com.fittrack.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 20) @Pattern(regexp = "") String username,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 8) String password
) {}
