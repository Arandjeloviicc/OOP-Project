package com.fittrack.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {

    private static Stage stage;

    public static void setStage(Stage s) {
        stage = s;
    }

    public static void switchTo(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getResource("/com/fittrack/view/" + fxml)
            );
            Parent root = loader.load();
            stage.getScene().setRoot(root);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
