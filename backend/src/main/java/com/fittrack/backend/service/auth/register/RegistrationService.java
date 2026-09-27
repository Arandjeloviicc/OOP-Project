package com.fittrack.backend.service.auth.register;

import com.fittrack.backend.repository.user.UserJdbcRepository;
import com.fittrack.backend.repository.user.projection.CreatedUser;
import com.fittrack.backend.repository.user.projection.RegistrationAvailability;
import com.fittrack.backend.security.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserJdbcRepository userJdbcRepository;

    public RegistrationResult register(String username, String email, String password) {
        String normalizedUsername = username.trim();
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        RegistrationAvailability availability = userJdbcRepository.checkRegistrationAvailability(
                normalizedUsername,
                normalizedEmail
        );

        if (availability.usernameTaken()) {
            return RegistrationResult.usernameTaken();
        }

        if (availability.emailTaken()) {
            return RegistrationResult.emailTaken();
        }

        String passwordHash = PasswordHasher.hash(password);

        CreatedUser createdUser = userJdbcRepository.create(
                normalizedUsername,
                normalizedEmail,
                passwordHash
        );

        return RegistrationResult.success(createdUser);
    }
}