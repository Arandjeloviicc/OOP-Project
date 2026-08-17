package com.fittrack.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class FxmlComponentLoader {

    private FxmlComponentLoader() {
    }

    public static <T> LoadedComponent<T> load(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    FxmlComponentLoader.class.getResource(
                            "/com/fittrack/view/" + fxml
                    )
            );

            Parent root = loader.load();
            T controller = loader.getController();

            return new LoadedComponent<>(root, controller);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load FXML component: " + fxml,
                    e
            );
        }
    }
}