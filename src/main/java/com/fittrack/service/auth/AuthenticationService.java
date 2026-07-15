package com.fittrack.service.auth;

import com.fittrack.dao.UserDAO;
import com.fittrack.model.User;

import java.util.Optional;

public class AuthenticationService {
    private final UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public AuthenticationResult login(String email, String password) {
        Optional<User> userOptional = userDAO.findByEmail(email);

        if (userOptional.isEmpty()) {
            return AuthenticationResult.userNotFound();
        }

        User user = userOptional.get();

        if(!verifyPassword(password, user.getPasswordHash())) {
            return AuthenticationResult.wrongPassword();
        }

        return AuthenticationResult.success(user);
    }

    private boolean verifyPassword(String password, String passwordHash) {
        // TODO: BCrypt check
        return password.equals(passwordHash);
    }
}