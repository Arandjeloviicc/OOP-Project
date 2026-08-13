package com.fittrack.backend.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    private static final int LOG_ROUNDS = 12;

    private PasswordHasher() {}

    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException(
                    "Password cannot be null."
            );
        }

        return BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS));
    }

    public static boolean matches(String password, String passwordHash) {
        if (password == null || passwordHash == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(password, passwordHash);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Stored password hash is invalid.",
                    exception
            );
        }
    }
}