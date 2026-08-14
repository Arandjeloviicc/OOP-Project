package com.fittrack.util;

import com.fittrack.model.view.ViewInstance;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class SceneManager {

    private static final Map<String, ViewInstance> viewCache = new HashMap<>();

    private static Stage stage;

    private SceneManager() {}

    public static void setStage(Stage s) {
        stage = s;
    }

    public static <T> T switchTo(String fxml) {
        try {
            ViewInstance viewInstance = viewCache.get(fxml);

            if (viewInstance == null) {
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
                Object controller = loader.getController();

                viewInstance = new ViewInstance(root, controller);
                viewCache.put(fxml, viewInstance);
            }

            stage.getScene().setRoot(viewInstance.root());

            return (T) viewInstance.controller();
        }
        catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load FXML: " + fxml,
                    e
            );
        }
    }
}
