package com.fittrack.app;

import com.fittrack.database.DatabaseConnection;
import com.fittrack.database.DatabaseInitializer;
import com.fittrack.util.AppConstants;
import com.fittrack.util.SceneManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage stage) throws IOException {

        Platform.setImplicitExit(true);

        SceneManager.setStage(stage);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "/com/fittrack/view/" + AppConstants.Views.MAIN_LAYOUT
                )
        );
        Scene scene = new Scene(loader.load(), 1280, 800);

        try {
            Image icon16 = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/fittrack/images/logos/app-logo-16.png")));
            Image icon32 = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/fittrack/images/logos/app-logo-32.png")));
            Image icon64 = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/fittrack/images/logos/app-logo-64.png")));

            stage.getIcons().addAll(icon16, icon32, icon64);

        } catch (RuntimeException exception) {
            log.info(
                    "Logo image not found: {}",
                    exception.getMessage()
            );
        }

        stage.setTitle("FitTrack");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.show();
    }

    @Override
    public void stop() {
        System.out.println("FitTrack application stopped.");
    }

    public static void main(String[] args) {
        System.out.println("Database location: " + DatabaseConnection.getDatabasePath());

        DatabaseInitializer.initialize();
        launch(args);
    }
}