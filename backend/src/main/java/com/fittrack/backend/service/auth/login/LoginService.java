package com.fittrack.backend.service.auth.login;

import com.fittrack.backend.repository.user.UserRepository;
import com.fittrack.backend.repository.user.projection.LoginData;
import com.fittrack.backend.security.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;

    public LoginResult login(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        Optional<LoginData> loginDataOptional = userRepository.findLoginDataByEmail(normalizedEmail);

        if (loginDataOptional.isEmpty()) {
            return LoginResult.userNotFound();
        }

        LoginData loginData = loginDataOptional.get();

        if (!PasswordHasher.matches(password, loginData.getPasswordHash())) {
            return LoginResult.wrongPassword();
        }

        return LoginResult.success(
                loginData.getId(),
                loginData.getUsername(),
                loginData.getEmail(),
                Boolean.TRUE.equals(
                        loginData.getProfileSetupComplete()
                )
        );
    }
}