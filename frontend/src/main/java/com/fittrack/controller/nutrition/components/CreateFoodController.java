package com.fittrack.controller.nutrition.components;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateFoodController implements Initializable {

    @FXML private VBox rootLayout;
    @FXML private ScrollPane setupScroll;
    @FXML private VBox createFoodContent;

    @FXML private Label titleMessage;

    @FXML private TextField nameField;
    @FXML private Label nameMessage;
    @FXML private TextField brandField;
    @FXML private TextField servingSizeField;
    @FXML private Label servingSizeMessage;
    @FXML private TextField caloriesField;
    @FXML private Label caloriesMessage;
    @FXML private TextField carbsField;
    @FXML private Label carbsMessage;
    @FXML private TextField fatField;
    @FXML private Label fatMessage;
    @FXML private TextField proteinField;
    @FXML private Label proteinMessage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Setup ScrollPane
        createFoodContent.minHeightProperty().bind(
                setupScroll.viewportBoundsProperty().map(Bounds::getHeight)
        );
    }

    @FXML
    private void handleCancel() {

    }

    @FXML
    private void handleCreate() {

    }
}
