package com.fittrack.ui.loader;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.net.URL;

public class FxmlComponentLoader {

    private FxmlComponentLoader() {}

    private static final String BASE_PATH = "/com/fittrack/view/";

    public static <T> LoadedComponent<T> load(String fxml) {
        String resourcePath = BASE_PATH + fxml;
        URL resource = FxmlComponentLoader.class.getResource(resourcePath);

        if (resource == null) {
            throw new IllegalStateException(
                    "FXML resource not found on classpath: " + resourcePath
            );
        }

        try {
            FXMLLoader loader = new FXMLLoader(resource);

            Parent root = loader.load();
            T controller = loader.getController();

            if (controller == null) {
                throw new IllegalStateException(
                        "FXML component has no controller: " + resourcePath
                );
            }

            return new LoadedComponent<>(root, controller);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to load FXML component '" + fxml + "' from " + resourcePath,
                    e
            );
        }
    }
}