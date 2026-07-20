package com.fittrack.service.auth;

import com.fittrack.dao.UserDAO;
import com.fittrack.model.user.User;
import com.fittrack.security.PasswordHasher;

public class RegistrationService {
    private final UserDAO userDAO;

    public RegistrationService() {
        this.userDAO = new UserDAO();
    }

    public RegistrationResult register(String username, String email, String password) {
        if(userDAO.existsByUsername(username)) {
            return RegistrationResult.usernameTaken();
        }

        if (userDAO.existsByEmail(email)) {
            return RegistrationResult.emailTaken();
        }

        String passwordHash = hashPassword(password);

        User createdUser = userDAO.save(username, email, passwordHash);

        return RegistrationResult.success(createdUser);
    }

    private String hashPassword(String password) {
        return PasswordHasher.hash(password);
    }
}