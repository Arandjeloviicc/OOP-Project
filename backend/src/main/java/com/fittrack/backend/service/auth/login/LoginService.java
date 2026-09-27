package com.fittrack.backend.service.auth.login;

import com.fittrack.backend.repository.user.UserJdbcRepository;
import com.fittrack.backend.repository.user.projection.LoginData;
import com.fittrack.backend.security.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserJdbcRepository userJdbcRepository;

    public LoginResult login(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        Optional<LoginData> loginDataOptional = userJdbcRepository.findLoginDataByEmail(normalizedEmail);

        if (loginDataOptional.isEmpty()) {
            return LoginResult.userNotFound();
        }

        LoginData loginData = loginDataOptional.get();

        if (!PasswordHasher.matches(password, loginData.passwordHash())) {
            return LoginResult.wrongPassword();
        }

        return LoginResult.success(
                loginData.id(),
                loginData.username(),
                loginData.email(),
                loginData.profileSetupComplete()
        );
    }
}