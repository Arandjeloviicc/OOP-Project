package com.fittrack.util;

public final class AppConstants {

    private AppConstants() {}

    public static final class Views {

        private Views() {}

        // Main Views
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


        // PopUp Views
        public static final String ADD_FOOD = "nutrition/add-food-view.fxml";
    }

    public static final class Components {

        private Components() {}

        public static final String NUTRITION_PROGRESS_CARD = "nutrition/components/nutrition-progress-card.fxml";
        public static final String MEAL_CARD = "nutrition/components/meal-card.fxml";
        public static final String FOOD_LIST_ITEM = "nutrition/components/food-list-item.fxml";
        public static final String NUTRITION_MACRO_PREVIEW = "nutrition/components/nutrition-macro-preview.fxml";
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

        // Calculators
        public static final int MIN_BODY_FAT_PERCENTAGE = 2;
        public static final int MAX_BODY_FAT_PERCENTAGE = 70;
        public static final int WEIGHT_LOSS_CALORIE_DEFICIT = 500;
        public static final int WEIGHT_GAIN_CALORIE_SURPLUS = 300;
        public static final int MIN_NECK_CIRCUMFERENCE = 20;
        public static final int MAX_NECK_CIRCUMFERENCE = 70;
        public static final int MIN_WAIST_CIRCUMFERENCE = 40;
        public static final int MAX_WAIST_CIRCUMFERENCE = 250;
        public static final int MIN_HIP_CIRCUMFERENCE = 50;
        public static final int MAX_HIP_CIRCUMFERENCE = 250;
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
        // Invalid
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

        public static final String INVALID_DATE_OF_BIRTH_FORMAT_MESSAGE =
                "Date must be in dd.MM.yyyy format.";

        public static final String INVALID_DATE_OF_BIRTH_AGE_MESSAGE =
                "Date of birth must indicate an age between %d and %d years."
                        .formatted(
                                AppConstants.Validation.MIN_AGE,
                                AppConstants.Validation.MAX_AGE
                        );
        public static final String INVALID_GENDER_MESSAGE = "Please select your gender.";

        // Helper
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
                "Use dd.MM.yyyy format. Age must be between %d and %d years."
                        .formatted(
                                AppConstants.Validation.MIN_AGE,
                                AppConstants.Validation.MAX_AGE
                        );

        // Fitness goals
        // Invalid
        public static final String INVALID_HEIGHT_MESSAGE = "Enter a height between %d and %d cm.".formatted(AppConstants.Validation.MIN_HEIGHT, AppConstants.Validation.MAX_HEIGHT);
        public static final String INVALID_WEIGHT_MESSAGE = "Enter a weight between %d and %d kg.".formatted(AppConstants.Validation.MIN_WEIGHT, AppConstants.Validation.MAX_WEIGHT);
        public static final String ACTIVITY_NOT_SELECTED_MESSAGE = "Select your activity level.";
        public static final String GOAL_NOT_SELECTED_MESSAGE = "Select a goal.";
        public static final String WEEKLY_GOAL_NOT_SELECTED_MESSAGE = "Select a weekly goal.";
        public static final String INVALID_GOAL_WEIGHT_LOSE_MESSAGE = "Goal weight should be lower than your current weight.";
        public static final String INVALID_GOAL_WEIGHT_GAIN_MESSAGE = "Goal weight should be higher than your current weight.";

        // Helpers
        public static final String HELPER_HEIGHT_MESSAGE = "Height in centimeters.";
        public static final String HELPER_WEIGHT_MESSAGE = "Current weight in kilograms.";
        public static final String HELPER_ACTIVITY_MESSAGE = "Your typical activity level.";
        public static final String HELPER_GOAL_TYPE_MESSAGE = "Your target goal.";
        public static final String HELPER_GOAL_WEIGHT_MESSAGE = "The weight you're aiming for (optional).";
        public static final String HELPER_WEEKLY_GOAL_MESSAGE = "Rate at which you want to reach your goal.";

        // Calculators
        // Invalid
        public static final String INVALID_AGE_MESSAGE =
                "Age must be between %d and %d years."
                        .formatted(
                                AppConstants.Validation.MIN_AGE,
                                AppConstants.Validation.MAX_AGE
                        );
        public static final String INVALID_BODY_FAT_MESSAGE =
                "Body fat must be between %d%% and %d%%."
                        .formatted(
                                AppConstants.Validation.MIN_BODY_FAT_PERCENTAGE,
                                AppConstants.Validation.MAX_BODY_FAT_PERCENTAGE
                        );
        public static final String INVALID_NECK_MESSAGE =
                "Neck circumference must be between %d and %d cm."
                        .formatted(
                                Validation.MIN_NECK_CIRCUMFERENCE,
                                Validation.MAX_NECK_CIRCUMFERENCE
                        );
        public static final String INVALID_NECK_WAIST_RELATION_MESSAGE =
                "Neck circumference must be smaller than waist circumference.";
        public static final String INVALID_WAIST_MESSAGE =
                "Waist circumference must be between %d and %d cm."
                        .formatted(
                                Validation.MIN_WAIST_CIRCUMFERENCE,
                                Validation.MAX_WAIST_CIRCUMFERENCE
                        );
        public static final String INVALID_HIP_MESSAGE =
                "Hip circumference must be between %d and %d cm."
                        .formatted(
                                Validation.MIN_HIP_CIRCUMFERENCE,
                                Validation.MAX_HIP_CIRCUMFERENCE
                        );

        // Helpers
        public static final String HELPER_AGE_MESSAGE = "Between %d and %d years.".formatted(AppConstants.Validation.MIN_AGE, AppConstants.Validation.MAX_AGE);
        public static final String HELPER_BODY_FAT_MESSAGE =
                "Optional. Between %d%% and %d%%."
                        .formatted(
                                AppConstants.Validation.MIN_BODY_FAT_PERCENTAGE,
                                AppConstants.Validation.MAX_BODY_FAT_PERCENTAGE
                        );
        public static final String HELPER_NECK_MESSAGE =
                "Enter a neck circumference between %d and %d cm."
                        .formatted(
                                Validation.MIN_NECK_CIRCUMFERENCE,
                                Validation.MAX_NECK_CIRCUMFERENCE
                        );
        public static final String HELPER_WAIST_MESSAGE =
                "Enter a waist circumference between %d and %d cm."
                        .formatted(
                                Validation.MIN_WAIST_CIRCUMFERENCE,
                                Validation.MAX_WAIST_CIRCUMFERENCE
                        );
        public static final String HELPER_HIP_MESSAGE =
                "Enter a hip circumference between %d and %d cm."
                        .formatted(
                                Validation.MIN_HIP_CIRCUMFERENCE,
                                Validation.MAX_HIP_CIRCUMFERENCE
                        );
    }
}