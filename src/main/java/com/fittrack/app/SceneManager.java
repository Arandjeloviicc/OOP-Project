package com.fittrack.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneManager {

    private SceneManager() {}

    private static Stage stage;

    public static void setStage(Stage s) {
        stage = s;
    }

    public static <T> T switchTo(String fxml) {
        try {
            URL resource = SceneManager.class.getResource(
                    "/com/fittrack/view/" + fxml
            );

            // Wrong FXML file name
            if(resource == null) {
                throw new IllegalArgumentException(
                        "FXML resource not found: " + fxml
                );
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            T controller = loader.getController();

            stage.getScene().setRoot(root);

            return controller;
        }
        catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load FXML: " + fxml,
                    e
            );
        }
    }
}
