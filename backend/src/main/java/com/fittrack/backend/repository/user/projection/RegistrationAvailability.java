package com.fittrack.backend.repository.user.projection;

public record RegistrationAvailability(
        boolean usernameTaken,
        boolean emailTaken
) {}