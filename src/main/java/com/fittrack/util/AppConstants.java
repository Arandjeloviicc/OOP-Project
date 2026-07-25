package com.fittrack.util;

public final class AppConstants {

    private AppConstants() {}

    public static final class Views {

        private Views() {}

        public static final String LOGIN = "auth/login-view.fxml";
        public static final String REGISTER = "auth/register-view.fxml";
        public static final String PROFILE_SETUP = "profile/profile-setup-view.fxml";
        public static final String MAIN_LAYOUT = "main/main-layout-view.fxml";
        public static final String DASHBOARD = "dashboard/dashboard-view.fxml";
        public static final String CALCULATORS = "calculator/calculators-view.fxml";
        public static final String MEALS = "nutrition/meals-view.fxml";
        public static final String WORKOUTS = "workout/workouts-view.fxml";
        public static final String MEASUREMENTS = "";
        public static final String USER_PROFILE = "profile/user-profile-view.fxml";
    }

    public static final class Validation {

        private Validation() {}

        // Login/Register view
        public static final int MIN_PASSWORD_LENGTH = 8;
        public static final int MAX_EMAIL_LENGTH = 254;
        public static final int MIN_USERNAME_LENGTH = 3;
        public static final int MAX_USERNAME_LENGTH = 20;

        // Profile setup view
        // Personal info
        public static final int MIN_AGE = 13;
        public static final int MAX_AGE = 120;
        public static final int MIN_NAME_LENGTH = 2;
        public static final int MAX_NAME_LENGTH = 50;

        // Fitness goals
        public static final int MIN_HEIGHT = 50;
        public static final int MAX_HEIGHT = 250;
        public static final int MIN_WEIGHT = 30;
        public static final int MAX_WEIGHT = 300;
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
        // Personal info
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

        // Fitness goals
        public static final String INVALID_HEIGHT_MESSAGE = "Enter a height between %d and %d cm.".formatted(AppConstants.Validation.MIN_HEIGHT, AppConstants.Validation.MAX_HEIGHT);
        public static final String INVALID_WEIGHT_MESSAGE = "Enter a weight between %d and %d kg.".formatted(AppConstants.Validation.MIN_WEIGHT, AppConstants.Validation.MAX_WEIGHT);
        public static final String ACTIVITY_NOT_SELECTED_MESSAGE = "Select your activity level.";
        public static final String GOAL_NOT_SELECTED_MESSAGE = "Select a goal.";
        public static final String WEEKLY_GOAL_NOT_SELECTED_MESSAGE = "Select a weekly goal.";
        public static final String INVALID_GOAL_WEIGHT_LOSE_MESSAGE = "Goal weight should be lower than your current weight.";
        public static final String INVALID_GOAL_WEIGHT_GAIN_MESSAGE = "Goal weight should be higher than your current weight.";

        public static final String HELPER_HEIGHT_MESSAGE = "Height in centimeters.";
        public static final String HELPER_WEIGHT_MESSAGE = "Current weight in kilograms.";
        public static final String HELPER_ACTIVITY_MESSAGE = "Your typical activity level.";
        public static final String HELPER_GOAL_TYPE_MESSAGE = "Your target goal.";
        public static final String HELPER_GOAL_WEIGHT_MESSAGE = "The weight you're aiming for (optional).";
        public static final String HELPER_WEEKLY_GOAL_MESSAGE = "Rate at which you want to reach your goal.";

        // Calculators
        public static final String INVALID_AGE_MESSAGE = "Age must be between %d and %d years.".formatted(AppConstants.Validation.MIN_AGE, AppConstants.Validation.MAX_AGE);
        public static final String HELPER_AGE_MESSAGE = "Between %d and %d years.".formatted(AppConstants.Validation.MIN_AGE, AppConstants.Validation.MAX_AGE);
    }
}
