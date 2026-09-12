package com.fittrack.backend.dto.profile;

import com.fittrack.backend.entity.profile.ActivityLevel;
import com.fittrack.backend.entity.profile.Gender;
import com.fittrack.backend.entity.profile.WeightGoal;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record ProfileSetupRequest(
        @NotNull
        @Positive
        Integer userId,

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
        double height,

        @NotNull
        ActivityLevel activityLevel,

        @NotNull
        WeightGoal goalType,

        @DecimalMin("30.0")
        @DecimalMax("300.0")
        Double goalWeight,

        @DecimalMin("0.25")
        @DecimalMax("1.0")
        Double weeklyGoal,

        @DecimalMin("30.0")
        @DecimalMax("300.0")
        double weight
) {}
