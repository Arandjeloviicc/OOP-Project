package com.fittrack.backend.service.auth;

import com.fittrack.backend.entity.user.User;
import com.fittrack.backend.repository.user.UserRepository;
import com.fittrack.backend.repository.user.projection.RegistrationAvailability;
import com.fittrack.backend.security.PasswordHasher;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class RegistrationService {

    private final UserRepository userRepository;

    public RegistrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public RegistrationResult register(String username, String email, String password) {
        String normalizedUsername = username.trim();
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        RegistrationAvailability availability = userRepository.checkRegistrationAvailability(
                normalizedUsername,
                normalizedEmail
        );

        if (Boolean.TRUE.equals(availability.getUsernameTaken())) {
            return RegistrationResult.usernameTaken();
        }

        if (Boolean.TRUE.equals(availability.getEmailTaken())) {
            return RegistrationResult.emailTaken();
        }

        String passwordHash = PasswordHasher.hash(password);

        User user = new User(normalizedUsername, normalizedEmail, passwordHash);

        User createdUser = userRepository.save(user);

        return RegistrationResult.success(createdUser);
    }
}