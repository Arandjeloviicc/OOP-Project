package com.fittrack.backend.service.auth;

import com.fittrack.backend.entity.user.User;
import com.fittrack.backend.repository.UserRepository;
import com.fittrack.backend.security.PasswordHasher;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final UserRepository userRepository;

    public RegistrationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public RegistrationResult register(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            return RegistrationResult.usernameTaken();
        }

        if (userRepository.existsByEmail(email)) {
            return RegistrationResult.emailTaken();
        }

        String passwordHash = PasswordHasher.hash(password);

        User user = new User(username, email, passwordHash);
        User createdUser = userRepository.save(user);

        return RegistrationResult.success(createdUser);
    }
}