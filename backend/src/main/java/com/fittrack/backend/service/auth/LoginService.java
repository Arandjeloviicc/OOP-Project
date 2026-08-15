package com.fittrack.backend.service.auth;

import com.fittrack.backend.entity.user.User;
import com.fittrack.backend.repository.user.UserRepository;
import com.fittrack.backend.security.PasswordHasher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {

    private final UserRepository userRepository;

    public LoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResult login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return LoginResult.userNotFound();
        }

        User user = userOptional.get();

        if (!PasswordHasher.matches(password, user.getPasswordHash())) {
            return LoginResult.wrongPassword();
        }

        return LoginResult.success(user);
    }
}