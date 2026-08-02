package com.fittrack.controller.calculator;

import com.fittrack.controller.common.FormController;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.model.calculators.CalculatorType;
import com.fittrack.model.profile.Gender;
import com.fittrack.util.AppConstants;
import com.fittrack.util.FitnessInputValidator;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class CalculatorsController extends FormController implements Initializable, ResponsiveLayout {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(CalculatorsController.class);

    @Override
    protected Logger getLogger() { return log; }

    @FXML private BorderPane rootLayout;
    @FXML private HBox calculatorTabs;
    @FXML private ScrollPane setupScroll;
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
    @FXML private ToggleGroup genderGroup;
    @FXML private ToggleButton maleButton;
    @FXML private ToggleButton femaleButton;
    @FXML private TextField heightField;
    @FXML private Label heightMessage;
    @FXML private TextField weightField;
    @FXML private Label weightMessage;

    // BMI
    @FXML private Label bmiValueLabel;
    @FXML private Label bmiStatusLabel;
    @FXML private Label healthyWeightRangeLabel;
    @FXML private Label bmiPrimeLabel;
    @FXML private Tooltip bmiPrimeTooltip;
    @FXML private Label ponderalIndexLabel;
    @FXML private Button bmiPonderalIndexLabel;
    @FXML private Tooltip bmiPonderalIndexTooltip;

    // Constants
    private static final int NARROW_BREAKPOINT = 550;

    // Is Narrow
    private Boolean narrowLayout;
    private List<ToggleButton> tabButtons;

    // PseudoClass for Narrow screen size
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Setup ScrollPane
        calculatorBodyStack.minHeightProperty().bind(
                setupScroll.viewportBoundsProperty().map(Bounds::getHeight)
        );

        tabButtons = List.of(bmiTab, bmrTab, tdeeTab);
        updateTabsLayout();

        setRootResponsiveRules();

        bmiTab.setUserData(CalculatorType.BMI);
        bmrTab.setUserData(CalculatorType.BMR);
        tdeeTab.setUserData(CalculatorType.TDEE);

        maleButton.setUserData(Gender.MALE);
        femaleButton.setUserData(Gender.FEMALE);

        setToolTipDelay();

        // Fields Messages and Listeners
        restoreAgeHelper();
        restoreHeightHelper();
        restoreWeightHelper();
        addListeners();
    }

    @FXML
    public void handleCalculate() {
        CalculatorType calculatorType = (CalculatorType) calculatorGroup.getSelectedToggle().getUserData();

        switch (calculatorType) {
            case BMI -> handleBMI();
            case BMR -> handleBMR();
            case TDEE -> handleTDEE();
        }
    }

    private void handleBMI() {
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

        double heightMeters = Double.parseDouble(height) / 100.0;
        double weightKg = Double.parseDouble(weight);

        setBmiResult(heightMeters, weightKg);
    }

    private void handleBMR() {
        log.info("BMR Calculate successful");
    }

    private void handleTDEE() {
        log.info("TDEE Calculate successful");
    }

    // ── Responsive Helpers ─────────────────────────────────────────────────
    @Override
    public void updateLayout(boolean narrow) {
        if (Objects.equals(narrowLayout, narrow)) return;

        narrowLayout = narrow;

        rootLayout.pseudoClassStateChanged(NARROW, narrow);

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

        updatePanelWidths(narrow);
    }

    // Toggle Buttons - Responsive
    private void updateTabsLayout() {
        for (ToggleButton tab : tabButtons) {
            HBox.setHgrow(tab, Priority.ALWAYS);
            tab.setMaxWidth(Double.MAX_VALUE);
        }
    }

    private void setRootResponsiveRules() {
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
    }

    private void updatePanelWidths(boolean narrow) {
        formPanel.prefWidthProperty().unbind();
        resultPanel.prefWidthProperty().unbind();

        if (!narrow) {
            DoubleBinding availableWidth = wideContainer.widthProperty()
                    .subtract(wideContainer.spacingProperty())
                    .subtract(wideContainer.getPadding().getLeft())
                    .subtract(wideContainer.getPadding().getRight());

            formPanel.prefWidthProperty().bind(availableWidth.multiply(0.40));
            resultPanel.prefWidthProperty().bind(availableWidth.multiply(0.60));
        }
    }

    private void addListeners() {
        ageField.textProperty().addListener((obs, oldValue, newValue) -> restoreAgeHelper());
        heightField.textProperty().addListener((obs, oldValue, newValue) -> restoreHeightHelper());
        weightField.textProperty().addListener((obs, oldValue, newValue) -> restoreWeightHelper());

        calculatorGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                calculatorGroup.selectToggle(oldToggle);
            }
        });

        genderGroup.selectedToggleProperty().addListener(
                (obs, oldToggle, newToggle) -> {
                    if (newToggle == null && oldToggle != null) {
                        genderGroup.selectToggle(oldToggle);
                    }
                }
        );
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

    // ── BMI Helpers ─────────────────────────────────────────────────
    private void setBmiResult(double heightMeters, double weightKg) {
        double heightSquared = heightMeters * heightMeters;
        double heightCubed = heightSquared * heightMeters;

        double exactBmi = weightKg / heightSquared;
        double bmi = Math.round(exactBmi * 10.0) / 10.0;
        bmiValueLabel.setText(String.format(Locale.US, "%.1f", bmi));

        bmiStatusLabel.getStyleClass().removeAll(
                "bmi-status-underweight",
                "bmi-status-normal",
                "bmi-status-overweight",
                "bmi-status-obese"
        );

        if (bmi < 18.5) {
            bmiStatusLabel.setText("Underweight");
            bmiStatusLabel.getStyleClass().add("bmi-status-underweight");
        } else if (bmi < 25.0) {
            bmiStatusLabel.setText("Normal");
            bmiStatusLabel.getStyleClass().add("bmi-status-normal");
        } else if (bmi < 30.0) {
            bmiStatusLabel.setText("Overweight");
            bmiStatusLabel.getStyleClass().add("bmi-status-overweight");
        } else {
            bmiStatusLabel.setText("Obese");
            bmiStatusLabel.getStyleClass().add("bmi-status-obese");
        }

        bmiStatusLabel.setVisible(true);
        bmiStatusLabel.setManaged(true);

        double minHealthyWeight = 18.5 * heightSquared;
        double maxHealthyWeight = 24.9 * heightSquared;
        healthyWeightRangeLabel.setText(String.format(Locale.US, "%.1f kg – %.1f kg", minHealthyWeight, maxHealthyWeight));

        double bmiPrime = bmi / 25;
        bmiPrimeLabel.setText(String.format(Locale.US, "%.1f", bmiPrime));

        double ponderalIndex = weightKg / heightCubed;
        ponderalIndexLabel.setText(String.format(Locale.US, "%.1f kg/m³", ponderalIndex));
    }

    private void setToolTipDelay() {
        bmiPrimeTooltip.setShowDelay(Duration.millis(250));
        bmiPrimeTooltip.setShowDuration(Duration.seconds(10));
        bmiPrimeTooltip.setHideDelay(Duration.millis(100));

        bmiPonderalIndexTooltip.setShowDelay(Duration.millis(250));
        bmiPonderalIndexTooltip.setShowDuration(Duration.seconds(10));
        bmiPonderalIndexTooltip.setHideDelay(Duration.millis(100));
    }
}