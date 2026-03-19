package com.fittrack.app;

import com.fittrack.database.DatabaseInitializer;
import com.fittrack.util.AppConstants;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        SceneManager.setStage(stage);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/fittrack/view/" + AppConstants.Views.LOGIN)
        );
        Scene scene = new Scene(loader.load(), 1280, 800);

        stage.setTitle("FitTrack");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        launch(args);
    }
}