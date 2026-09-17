package com.fittrack.dto.profile.editor;

import java.time.LocalDate;

public record PersonalInfoUpdateRequest(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        double height
) {}