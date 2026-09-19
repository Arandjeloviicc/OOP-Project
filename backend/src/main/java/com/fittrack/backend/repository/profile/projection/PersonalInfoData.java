package com.fittrack.backend.repository.profile.projection;

import com.fittrack.backend.entity.profile.Gender;

import java.time.LocalDate;

public record PersonalInfoData(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Gender gender,
        double height
) {}
