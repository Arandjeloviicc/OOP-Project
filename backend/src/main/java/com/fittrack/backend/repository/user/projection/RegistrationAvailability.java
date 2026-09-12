package com.fittrack.backend.repository.user.projection;

public interface RegistrationAvailability {
    Boolean getUsernameTaken();

    Boolean getEmailTaken();
}
