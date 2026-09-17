package com.fittrack.backend.dto.profile.editor;

import com.fittrack.backend.entity.profile.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record PersonalInfoUpdateRequest(
        @NotBlank
        @Size(min = 2, max = 50)
        @Pattern(regexp = "^\\p{L}[\\p{L} '\\-]*\\p{L}$")
        String firstName,

        @NotBlank
        @Size(min = 2, max = 50)
        @Pattern(regexp = "^\\p{L}[\\p{L} '\\-]*\\p{L}$")
        String lastName,

        @NotNull
        @Past
        LocalDate dateOfBirth,

        @NotNull
        Gender gender,

        @DecimalMin("50.0")
        @DecimalMax("250.0")
        double height
) {}