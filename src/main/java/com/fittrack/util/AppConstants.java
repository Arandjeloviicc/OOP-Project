package com.fittrack.util;

public final class AppConstants {

    private AppConstants() {}

    public static final class Images {

        private Images() {}

        public static final String LOGIN_REGISTER_BG = "login-register-bg-2.png";
        public static final String APP_ICON = "app-logo-transparent-2.png";
    }

    public static final class Views {

        private Views() {}

        public static final String LOGIN = "Login_Register/login-view.fxml";
        public static final String REGISTER = "Login_Register/register-view.fxml";
        public static final String DASHBOARD = "dashboard-view.fxml";
        public static final String PROFILE_SETUP = "profile-setup-view.fxml";
    }

    public static final class Validation {

        private Validation() {}

        // Login/Register view
        public static final int MIN_PASSWORD_LENGTH = 8;
        public static final int MAX_EMAIL_LENGTH = 254;
        public static final int MIN_USERNAME_LENGTH = 3;
        public static final int MAX_USERNAME_LENGTH = 20;

        // Profile setup view
        public static final int MIN_AGE = 13;
        public static final int MAX_AGE = 120;
        public static final int MIN_NAME_LENGTH = 2;
        public static final int MAX_NAME_LENGTH = 50;
    }

    public static final class Messages {

        private Messages() {}

        // Login/Register view
        public static final String INVALID_USERNAME_MESSAGE =
                "Username must contain %d–%d characters and start with a letter."
                        .formatted(
                                AppConstants.Validation.MIN_USERNAME_LENGTH,
                                AppConstants.Validation.MAX_USERNAME_LENGTH
                        );
        public static final String INVALID_EMAIL_MESSAGE = "Enter a valid email address, e.g. name@example.com.";
        public static final String INVALID_PASSWORD_MESSAGE =
                "Password must contain at least %d characters."
                        .formatted(
                                AppConstants.Validation.MIN_PASSWORD_LENGTH
                        );
        public static final String HELPER_USERNAME_MESSAGE = "%d–%d characters; start with a letter.".formatted(AppConstants.Validation.MIN_USERNAME_LENGTH, AppConstants.Validation.MAX_USERNAME_LENGTH);
        public static final String HELPER_EMAIL_MESSAGE = "Use a valid address, e.g. name@example.com.";
        public static final String HELPER_PASSWORD_MESSAGE = "At least %d characters".formatted(AppConstants.Validation.MIN_PASSWORD_LENGTH);

        // Profile setup view
        public static final String INVALID_FIRST_NAME_MESSAGE =
                "First name must contain %d–%d characters and use only letters, spaces, hyphens, or apostrophes."
                        .formatted(
                                AppConstants.Validation.MIN_NAME_LENGTH,
                                AppConstants.Validation.MAX_NAME_LENGTH
                        );

        public static final String INVALID_LAST_NAME_MESSAGE =
                "Last name must contain %d–%d characters and use only letters, spaces, hyphens, or apostrophes."
                        .formatted(
                                AppConstants.Validation.MIN_NAME_LENGTH,
                                AppConstants.Validation.MAX_NAME_LENGTH
                        );

        public static final String INVALID_DATE_OF_BIRTH_MESSAGE =
                "Date of birth must indicate an age between %d and %d years."
                        .formatted(
                                AppConstants.Validation.MIN_AGE,
                                AppConstants.Validation.MAX_AGE
                        );
        public static final String INVALID_GENDER_MESSAGE = "Please select your gender.";

        public static final String HELPER_FIRST_NAME_MESSAGE =
                "%d–%d characters; letters, spaces, hyphens, and apostrophes allowed."
                        .formatted(
                                AppConstants.Validation.MIN_NAME_LENGTH,
                                AppConstants.Validation.MAX_NAME_LENGTH
                        );

        public static final String HELPER_LAST_NAME_MESSAGE =
                "%d–%d characters; letters, spaces, hyphens, and apostrophes allowed."
                        .formatted(
                                AppConstants.Validation.MIN_NAME_LENGTH,
                                AppConstants.Validation.MAX_NAME_LENGTH
                        );

        public static final String HELPER_DATE_OF_BIRTH_MESSAGE =
                "Age must be between %d and %d years."
                        .formatted(
                                AppConstants.Validation.MIN_AGE,
                                AppConstants.Validation.MAX_AGE
                        );
    }
}
