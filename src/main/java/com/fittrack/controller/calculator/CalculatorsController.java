package com.fittrack.controller.calculator;

import com.fittrack.controller.common.FormController;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.util.AppConstants;
import com.fittrack.util.FitnessInputValidator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class CalculatorsController extends FormController implements Initializable, ResponsiveLayout {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(CalculatorsController.class);

    @Override
    protected Logger getLogger() { return log; }

    @FXML private BorderPane rootLayout;
    @FXML private FlowPane calculatorForms;
    @FXML private HBox calculatorTabs;

    @FXML private StackPane calculatorBodyStack;
    @FXML private HBox wideContainer;
    @FXML private VBox narrowContainer;
    @FXML private VBox formPanel;
    @FXML private VBox resultPanel;

    @FXML private ToggleGroup calculatorGroup;
    @FXML private ToggleButton bmiTab;
    @FXML private ToggleButton bmrTab;
    @FXML private ToggleButton tdeeTab;

    @FXML private VBox ageGroup;
    @FXML private TextField ageField;
    @FXML private Label ageMessage;
    @FXML private TextField heightField;
    @FXML private Label heightMessage;
    @FXML private TextField weightField;
    @FXML private Label weightMessage;

    // Constants
    private static final int NARROW_BREAKPOINT = 500;

    // Is Narrow
    private Boolean narrowLayout;
    private List<ToggleButton> tabButtons;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tabButtons = List.of(bmiTab, bmrTab, tdeeTab);
        updateTabsLayout();

        HBox.setHgrow(formPanel, Priority.ALWAYS);
        HBox.setHgrow(resultPanel, Priority.ALWAYS);

        bindEqualWidths();

        formPanel.setMaxWidth(Double.MAX_VALUE);
        resultPanel.setMaxWidth(Double.MAX_VALUE);

        rootLayout.widthProperty().addListener((obs, oldW, newW) -> {
            double width = newW.doubleValue();
            if (width > 50) {
                updateLayout(width < NARROW_BREAKPOINT);
            }
        });

        Platform.runLater(() -> {
            double width = rootLayout.getWidth();
            if (width > 50) {
                updateLayout(width < NARROW_BREAKPOINT);
            }
        });

        // Fields Messages and Listeners
        restoreAgeHelper();
        restoreHeightHelper();
        restoreWeightHelper();
        addListeners();
    }

    @FXML
    public void handleCalculate() {
        String age = ageField.getText().trim();
        String height = heightField.getText().trim();
        String weight = weightField.getText().trim();

        boolean valid = true;

        if(!FitnessInputValidator.isAgeValid(age)) {
            showAgeMessage();
            shake(ageField);
            valid = false;
        }

        if(!FitnessInputValidator.isHeightValid(height)) {
            showHeightMessage();
            shake(heightField);
            valid = false;
        }

        if(!FitnessInputValidator.isWeightValid(weight)) {
            showWeightMessage();
            shake(weightField);
            valid = false;
        }

        if(!valid) return;

        log.info("Calculate successful");
    }

    // ── Responsive Helpers ─────────────────────────────────────────────────
    @Override
    public void updateLayout(boolean narrow) {
        if (Objects.equals(narrowLayout, narrow)) return;
        narrowLayout = narrow;

        if (narrow) {
            wideContainer.getChildren().clear();
            narrowContainer.getChildren().setAll(formPanel, resultPanel);

            wideContainer.setVisible(false);
            wideContainer.setManaged(false);
            narrowContainer.setVisible(true);
            narrowContainer.setManaged(true);
        } else {
            narrowContainer.getChildren().clear();
            wideContainer.getChildren().setAll(formPanel, resultPanel);

            narrowContainer.setVisible(false);
            narrowContainer.setManaged(false);
            wideContainer.setVisible(true);
            wideContainer.setManaged(true);
        }
    }

    // Toggle Buttons - Responsive
    private void updateTabsLayout() {
        for (ToggleButton tab : tabButtons) {
            HBox.setHgrow(tab, Priority.ALWAYS);
            tab.setMaxWidth(Double.MAX_VALUE);
        }
    }

    // Calculator Body - 50% of width
    private void bindEqualWidths() {
        formPanel.prefWidthProperty().unbind();
        resultPanel.prefWidthProperty().unbind();

        formPanel.prefWidthProperty().bind(wideContainer.widthProperty().multiply(0.55));
        resultPanel.prefWidthProperty().bind(wideContainer.widthProperty().multiply(0.45));
    }

    private void addListeners() {
        ageField.textProperty().addListener((obs, oldValue, newValue) -> restoreAgeHelper());
        heightField.textProperty().addListener((obs, oldValue, newValue) -> restoreHeightHelper());
        weightField.textProperty().addListener((obs, oldValue, newValue) -> restoreWeightHelper());

        // I will remove this
        calculatorGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            boolean isBmi = newToggle == bmiTab;

            ageGroup.setVisible(!isBmi);
            ageGroup.setManaged(!isBmi);
        });
    }

    // ── Age Helpers ─────────────────────────────────────────────────
    private void showAgeMessage() {
        setFieldMessage(ageMessage, AppConstants.Messages.INVALID_AGE_MESSAGE, true, ageField);
    }

    private void restoreAgeHelper() {
        setFieldMessage(ageMessage, AppConstants.Messages.HELPER_AGE_MESSAGE, false, ageField);
    }

    // ── Height Helpers ─────────────────────────────────────────────────
    private void showHeightMessage() {
        setFieldMessage(heightMessage, AppConstants.Messages.INVALID_HEIGHT_MESSAGE, true, heightField);
    }

    private void restoreHeightHelper() {
        setFieldMessage(heightMessage, AppConstants.Messages.HELPER_HEIGHT_MESSAGE, false, heightField);
    }

    // ── Weight Helpers ─────────────────────────────────────────────────
    private void showWeightMessage() {
        setFieldMessage(weightMessage, AppConstants.Messages.INVALID_WEIGHT_MESSAGE, true, weightField);
    }

    private void restoreWeightHelper() {
        setFieldMessage(weightMessage, AppConstants.Messages.HELPER_WEIGHT_MESSAGE, false, weightField);
    }
}
