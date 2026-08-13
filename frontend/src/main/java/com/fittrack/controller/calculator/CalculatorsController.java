package com.fittrack.controller.calculator;

import com.fittrack.controller.common.FormController;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.model.calculators.CalculatorType;
import com.fittrack.model.profile.ActivityLevel;
import com.fittrack.model.profile.Gender;
import com.fittrack.util.AppConstants;
import com.fittrack.util.FitnessInputValidator;
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

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class CalculatorsController extends FormController implements Initializable, ResponsiveLayout {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(CalculatorsController.class);

    @Override
    protected Logger getLogger() { return log; }

    // Layouts
    @FXML private BorderPane rootLayout;
    @FXML private ScrollPane setupScroll;
    @FXML private StackPane calculatorBodyStack;

    // Panels
    @FXML private HBox wideContainer;
    @FXML private VBox narrowContainer;
    @FXML private VBox formPanel;
    @FXML private VBox resultPanel;

    // Calculator type Toggle Buttons
    @FXML private ToggleGroup calculatorGroup;
    @FXML private ToggleButton bmiTab;
    @FXML private ToggleButton bmrTab;
    @FXML private ToggleButton tdeeTab;

    // Form
    @FXML private VBox ageGroup;
    @FXML private TextField ageField;
    @FXML private Label ageMessage;
    @FXML private VBox genderGroupContainer;
    @FXML private ToggleGroup genderGroup;
    @FXML private ToggleButton maleButton;
    @FXML private ToggleButton femaleButton;
    @FXML private TextField heightField;
    @FXML private Label heightMessage;
    @FXML private TextField weightField;
    @FXML private Label weightMessage;
    @FXML private VBox activityLevelGroup;
    @FXML private ComboBox<ActivityLevel> activityLevelComboBox;
    @FXML private Label activityLevelMessage;
    @FXML private VBox bodyFatSettingsGroup;
    @FXML private Label bodyFatToggleLabel;
    @FXML private VBox bodyFatFieldGroup;
    @FXML private TextField bodyFatField;
    @FXML private Label bodyFatMessage;

    // BMI
    @FXML private VBox bmiResultView;
    @FXML private Label bmiValueLabel;
    @FXML private Label bmiStatusLabel;
    @FXML private Label healthyWeightRangeLabel;
    @FXML private Label bmiPrimeLabel;
    @FXML private Tooltip bmiPrimeTooltip;
    @FXML private Label ponderalIndexLabel;
    @FXML private Tooltip bmiPonderalIndexTooltip;

    // BMR
    @FXML private VBox bmrResultView;
    @FXML private Label bmrValueLabel;

    // TDEE
    @FXML private VBox tdeeResultView;
    @FXML private Label tdeeValueLabel;
    @FXML private Label tdeeActivityLevelLabel;
    @FXML private Label tdeeBmrValueLabel;
    @FXML private Label weightLossCaloriesLabel;
    @FXML private Label maintenanceCaloriesLabel;
    @FXML private Label weightGainCaloriesLabel;

    // Constants
    private static final int NARROW_BREAKPOINT = 550;

    // Is Narrow
    private Boolean narrowLayout;
    private List<ToggleButton> tabButtons;

    // PseudoClass for Narrow screen size
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");

    // Adding PseudoClass to ComboBox (Change text color when nothing is selected)
    private static final PseudoClass NO_SELECTION = PseudoClass.getPseudoClass("no-selection");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Setup ScrollPane
        calculatorBodyStack.minHeightProperty().bind(
                setupScroll.viewportBoundsProperty().map(Bounds::getHeight)
        );

        // Initialize all form controls
        initializeCalculatorControls();

        // Responsive initialize
        updateTabsLayout();
        initializeResponsiveLayout(rootLayout, NARROW_BREAKPOINT);

        // ToolTip delay after hovering
        setToolTipDelay();

        // Fields Messages
        restoreAgeHelper();
        restoreHeightHelper();
        restoreWeightHelper();
        restoreActivityLevelHelper();
        restoreBodyFatHelper();

        // Listeners
        addListeners();
    }

    private boolean validateInputs() {
        CalculatorType calculatorType = (CalculatorType) calculatorGroup.getSelectedToggle().getUserData();
        String age = ageField.getText().trim();
        String height = heightField.getText().trim();
        String weight = weightField.getText().trim();
        String bodyFat = bodyFatField.getText().trim();
        ActivityLevel activityLevel = activityLevelComboBox.getSelectionModel().getSelectedItem();

        boolean valid = true;

        if (calculatorType != CalculatorType.BMI
            && !FitnessInputValidator.isAgeValid(age)) {
            showAgeMessage();
            shake(ageField);
            valid = false;
        }

        if (!FitnessInputValidator.isHeightValid(height)) {
            showHeightMessage();
            shake(heightField);
            valid = false;
        }

        if (!FitnessInputValidator.isWeightValid(weight)) {
            showWeightMessage();
            shake(weightField);
            valid = false;
        }

        if (calculatorType != CalculatorType.BMI
            && !bodyFat.isEmpty()
            && !FitnessInputValidator.isBodyFatValid(bodyFat)) {
            showBodyFatMessage();
            shake(bodyFatField);
            valid = false;
        }

        if (calculatorType == CalculatorType.TDEE
        && activityLevel == null) {
            showActivityLevelMessage();
            shake(activityLevelComboBox);
            valid = false;
        }

        return valid;
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
        if(!validateInputs()) return;

        String height = heightField.getText().trim();
        String weight = weightField.getText().trim();

        double heightMeters = Double.parseDouble(height) / 100.0;
        double weightKg = Double.parseDouble(weight);

        setBmiResult(heightMeters, weightKg);
        showResult(CalculatorType.BMI);
    }

    private void handleBMR() {
        if(!validateInputs()) return;

        String age = ageField.getText().trim();
        Gender gender = (Gender) genderGroup.getSelectedToggle().getUserData();
        String height = heightField.getText().trim();
        String weight = weightField.getText().trim();
        String bodyFatText = bodyFatField.getText().trim();

        int ageInt = Integer.parseInt(age);
        double heightCm = Double.parseDouble(height);
        double weightKg = Double.parseDouble(weight);
        Double bodyFatPercentage = bodyFatText.isEmpty()
                ? null
                : Double.parseDouble(bodyFatText);

        setBmrResult(ageInt, gender, heightCm, weightKg, bodyFatPercentage);
        showResult(CalculatorType.BMR);
    }

    private void handleTDEE() {
        if(!validateInputs()) return;

        String age = ageField.getText().trim();
        Gender gender = (Gender) genderGroup.getSelectedToggle().getUserData();
        String height = heightField.getText().trim();
        String weight = weightField.getText().trim();
        ActivityLevel activityLevel = activityLevelComboBox.getSelectionModel().getSelectedItem();
        String bodyFatText = bodyFatField.getText().trim();

        int ageInt = Integer.parseInt(age);
        double heightCm = Double.parseDouble(height);
        double weightKg = Double.parseDouble(weight);
        Double bodyFatPercentage = bodyFatText.isEmpty()
                ? null
                : Double.parseDouble(bodyFatText);

        setTdeeResult(ageInt, gender, heightCm, weightKg, activityLevel, bodyFatPercentage);
        showResult(CalculatorType.TDEE);
    }

    @FXML
    private void handleBodyFatToggle() {
        boolean showField = !bodyFatFieldGroup.isVisible();

        bodyFatFieldGroup.setVisible(showField);
        bodyFatFieldGroup.setManaged(showField);

        bodyFatToggleLabel.setText(
                showField
                        ? "− Remove body fat percentage"
                        : "+ Add body fat percentage"
        );

        if (!showField) {
            bodyFatField.clear();
            restoreBodyFatHelper();
        }
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

    private void updateTabsLayout() {
        for (ToggleButton tab : tabButtons) {
            HBox.setHgrow(tab, Priority.ALWAYS);
            tab.setMaxWidth(Double.MAX_VALUE);
        }
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

    // ── Initialize Helpers ─────────────────────────────────────────────────
    private void addListeners() {
        ageField.textProperty().addListener((obs, oldValue, newValue) -> restoreAgeHelper());
        heightField.textProperty().addListener((obs, oldValue, newValue) -> restoreHeightHelper());
        weightField.textProperty().addListener((obs, oldValue, newValue) -> restoreWeightHelper());
        bodyFatField.textProperty().addListener((obs, oldValue, newValue) -> restoreBodyFatHelper());
        activityLevelComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            activityLevelComboBox.pseudoClassStateChanged(NO_SELECTION, newVal == null);
            restoreActivityLevelHelper();
        });

        calculatorGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                if (oldToggle != null) {
                    calculatorGroup.selectToggle(oldToggle);
                }
                return;
            }

            CalculatorType calculatorType =
                    (CalculatorType) newToggle.getUserData();

            updateCalculatorForm(calculatorType);
        });

        genderGroup.selectedToggleProperty().addListener(
                (obs, oldToggle, newToggle) -> {
                    if (newToggle == null && oldToggle != null) {
                        genderGroup.selectToggle(oldToggle);
                    }
                }
        );
    }

    private void initializeCalculatorControls() {
        tabButtons = List.of(bmiTab, bmrTab, tdeeTab);

        bmiTab.setUserData(CalculatorType.BMI);
        bmrTab.setUserData(CalculatorType.BMR);
        tdeeTab.setUserData(CalculatorType.TDEE);
        calculatorGroup.selectToggle(bmiTab);

        maleButton.setUserData(Gender.MALE);
        femaleButton.setUserData(Gender.FEMALE);
        genderGroup.selectToggle(maleButton);

        activityLevelComboBox.getItems().setAll(ActivityLevel.values());
        activityLevelComboBox.pseudoClassStateChanged(NO_SELECTION, activityLevelComboBox.getValue() == null);

        updateCalculatorForm(CalculatorType.BMI);
    }

    // ── Calculator Helpers ─────────────────────────────────────────────────
    private void updateCalculatorForm(CalculatorType calculatorType) {
        boolean isBmi = calculatorType == CalculatorType.BMI;
        boolean isTdee = calculatorType == CalculatorType.TDEE;

        ageGroup.setVisible(!isBmi);
        ageGroup.setManaged(!isBmi);

        genderGroupContainer.setVisible(!isBmi);
        genderGroupContainer.setManaged(!isBmi);

        bodyFatSettingsGroup.setVisible(!isBmi);
        bodyFatSettingsGroup.setManaged(!isBmi);

        activityLevelGroup.setVisible(isTdee);
        activityLevelGroup.setManaged(isTdee);
    }

    private void showResult(CalculatorType calculatorType) {
        boolean showBmi = calculatorType == CalculatorType.BMI;
        boolean showBmr = calculatorType == CalculatorType.BMR;
        boolean showTdee = calculatorType == CalculatorType.TDEE;

        bmiResultView.setVisible(showBmi);
        bmiResultView.setManaged(showBmi);

        bmrResultView.setVisible(showBmr);
        bmrResultView.setManaged(showBmr);

        tdeeResultView.setVisible(showTdee);
        tdeeResultView.setManaged(showTdee);
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

    // ── BMR Helpers ─────────────────────────────────────────────────
    private void setBmrResult(int age, Gender gender, double heightCm, double weightKg, Double bodyFatPercentage) {

        double bmr = calculateBmr(age, gender, heightCm, weightKg, bodyFatPercentage);

        int bmrRounded = (int) Math.round(bmr);

        bmrValueLabel.setText(String.valueOf(bmrRounded));
    }

    private double calculateBmr(int age, Gender gender, double heightCm, double weightKg, Double bodyFatPercentage) {
        if (bodyFatPercentage != null) {
            return calculateBmrWithBodyFat(weightKg, bodyFatPercentage);
        }

        return calculateStandardBmr(age, gender, heightCm, weightKg);
    }

    private double calculateBmrWithBodyFat(double weightKg, double bodyFatPercentage) {
        double leanBodyMass = weightKg * (1 - bodyFatPercentage / 100.0);

        return 370 + 21.6 * leanBodyMass;
    }

    private double calculateStandardBmr(int age, Gender gender, double heightCm, double weightKg) {
        double bmrBase = 10*weightKg + 6.25*heightCm - 5*age;

        return (gender == Gender.MALE)
                ? bmrBase + 5
                : bmrBase - 161;
    }

    // ── TDEE Helpers ─────────────────────────────────────────────────
    private void setTdeeResult(int age, Gender gender, double heightCm, double weightKg, ActivityLevel activityLevel, Double bodyFatPercentage) {

        double bmr = calculateBmr(age, gender, heightCm, weightKg, bodyFatPercentage);
        double tdee = bmr * activityLevel.getMultiplier();

        int bmrRounded = (int) Math.round(bmr);
        int tdeeRounded = (int) Math.round(tdee);

        int weightLossCalories = tdeeRounded - AppConstants.Validation.WEIGHT_LOSS_CALORIE_DEFICIT;
        int weightGainCalories = tdeeRounded + AppConstants.Validation.WEIGHT_GAIN_CALORIE_SURPLUS;

        tdeeValueLabel.setText(String.valueOf(tdeeRounded));
        tdeeActivityLevelLabel.setText(activityLevel.toString());
        tdeeBmrValueLabel.setText(String.format("%d kcal/day", bmrRounded));
        weightLossCaloriesLabel.setText(String.format("%d kcal/day", weightLossCalories));
        maintenanceCaloriesLabel.setText(String.format("%d kcal/day", tdeeRounded));
        weightGainCaloriesLabel.setText(String.format("%d kcal/day", weightGainCalories));
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

    // ── Activity level Helpers ─────────────────────────────────────────────────
    private void showActivityLevelMessage() {
        setFieldMessage(activityLevelMessage, AppConstants.Messages.ACTIVITY_NOT_SELECTED_MESSAGE, true, activityLevelComboBox);
    }

    private void restoreActivityLevelHelper() {
        setFieldMessage(activityLevelMessage, AppConstants.Messages.HELPER_ACTIVITY_MESSAGE, false, activityLevelComboBox);
    }

    // ── Body Fat Helpers ─────────────────────────────────────────────────
    private void showBodyFatMessage() {
        setFieldMessage(bodyFatMessage, AppConstants.Messages.INVALID_BODY_FAT_MESSAGE, true, bodyFatField);
    }

    private void restoreBodyFatHelper() {
        setFieldMessage(bodyFatMessage, AppConstants.Messages.HELPER_BODY_FAT_MESSAGE, false, bodyFatField);
    }
}