package com.fittrack.util;

import javafx.scene.image.Image;

import java.net.URL;

public final class AppImages {

    // Not used currently
    public static final Image LOGIN_REGISTER_BG = load("/com/fittrack/images/login-register-bg-2.png");
    public static final Image APP_LOGO = load("/com/fittrack/images/app-logo-transparent-2.png");

    public static final Image DASHBOARD_ICON = load("/com/fittrack/images/icons/dashboard.png");
    public static final Image CALCULATORS_ICON = load("/com/fittrack/images/icons/calculators.png");
    public static final Image MEALS_ICON = load("/com/fittrack/images/icons/meals.png");
    public static final Image WORKOUTS_ICON = load("/com/fittrack/images/icons/workouts.png");
    public static final Image MEASUREMENTS_ICON = load("/com/fittrack/images/icons/measurements.png");
    public static final Image USER_PROFILE_ICON = load("/com/fittrack/images/icons/profile.png");

    private AppImages() {}

    private static Image load(String path) {
        URL resource = AppImages.class.getResource(path);

        if (resource == null) {
            throw new IllegalArgumentException(
                    "Image resource not found: " + path
            );
        }

        return new Image(resource.toExternalForm());
    }

}
