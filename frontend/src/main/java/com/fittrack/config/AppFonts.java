package com.fittrack.config;

import javafx.scene.text.Font;

import java.io.InputStream;

public class AppFonts {

    private AppFonts() {}

    private static final String FONT_DIR = "/fonts/";

    private static final String INTER_REGULAR = FONT_DIR + "Inter_18pt-Regular.ttf";
    private static final String INTER_MEDIUM = FONT_DIR + "Inter_18pt-Medium.ttf";
    private static final String INTER_SEMIBOLD = FONT_DIR + "Inter_18pt-SemiBold.ttf";
    private static final String INTER_BOLD = FONT_DIR + "Inter_18pt-Bold.ttf";

    private static final double LOAD_SIZE = 12;

    public static void load() {
        loadFont(INTER_REGULAR);
        loadFont(INTER_MEDIUM);
        loadFont(INTER_SEMIBOLD);
        loadFont(INTER_BOLD);
    }

    private static void loadFont(String path) {
        InputStream stream = AppFonts.class.getResourceAsStream(path);

        if (stream == null) {
            throw new IllegalStateException("Font resource not found: " + path);
        }

        Font font = Font.loadFont(
                stream,
                LOAD_SIZE
        );

        if (font == null) {
            throw new IllegalStateException("Failed to load font: " + path);
        }
    }
}