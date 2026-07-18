package com.fittrack.session;

import com.fittrack.model.User;

public final class UserSession {

    private static final UserSession INSTANCE = new UserSession();

    private User currentUser;

    private UserSession() {
    }

    public static UserSession getInstance() {
        return INSTANCE;
    }

    public void start(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        if (currentUser != null) {
            throw new IllegalStateException("A user is already logged in.");
        }

        currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public User requireCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("No user is currently logged in.");
        }

        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void end() {
        currentUser = null;
    }
}