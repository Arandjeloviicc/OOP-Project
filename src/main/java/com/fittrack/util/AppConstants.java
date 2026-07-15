package com.fittrack.util;

public final class AppConstants {

    private AppConstants() {}

    public static final class Images {

        private Images() {}

        // TODO
        public static final String LOGIN_REGISTER_BG = "login-register-bg-2.png";
    }

    public static final class Views {

        private Views() {}

        public static final String LOGIN = "login-view.fxml";
        public static final String REGISTER = "register-view.fxml";
        public static final String DASHBOARD = "dashboard-view.fxml";
    }

    public static final class Validation {

        private Validation() {}

        public static final int MIN_PASSWORD_LENGTH = 8;
        public static final int MAX_EMAIL_LENGTH = 254;
        public static final int MIN_USERNAME_LENGTH = 3;
        public static final int MAX_USERNAME_LENGTH = 20;
    }
}
