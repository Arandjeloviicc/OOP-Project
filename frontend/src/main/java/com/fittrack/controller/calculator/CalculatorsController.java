package com.fittrack.controller.calculator;

import com.fittrack.util.NumberUtils;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.fittrack.controller.common.FormController;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.model.calculators.CalculatorType;
import com.fittrack.model.calculators.EnergyMode;
import com.fittrack.model.profile.Gender;
import com.fittrack.service.calculator.CalculationService;
import com.fittrack.util.AppConstants;
import com.fittrack.util.FitnessInputValidator;
import javafx.beans.binding.DoubleBinding;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
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
    @FXML private ToggleButton tdeeTab;
    @FXML private ToggleButton bodyFatTab;

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
    @FXML private ComboBox<EnergyMode> activityLevelComboBox;
    @FXML private Label activityLevelMessage;
    @FXML private VBox bodyFatSettingsGroup;
    @FXML private Label bodyFatToggleLabel;
    @FXML private VBox bodyFatFieldGroup;
    @FXML private TextField bodyFatField;
    @FXML private Label bodyFatMessage;
    @FXML private VBox bodyFatInputFields;
    @FXML private TextField neckField;
    @FXML private Label neckMessage;
    @FXML private TextField waistField;
    @FXML private Label waistMessage;
    @FXML private VBox hipGroup;
    @FXML private TextField hipField;
    @FXML private Label hipMessage;

    // BMI
    @FXML private VBox bmiResultView;
    @FXML private Label bmiValueLabel;
    @FXML private Label bmiStatusLabel;
    @FXML private Label healthyWeightRangeLabel;
    @FXML private Label bmiPrimeLabel;
    @FXML private Tooltip bmiPrimeTooltip;
    @FXML private Label ponderalIndexLabel;
    @FXML private Tooltip bmiPonderalIndexTooltip;
    @FXML private Label underweightBmiCategoryLabel;
    @FXML private Label normalBmiCategoryLabel;
    @FXML private Label overweightBmiCategoryLabel;
    @FXML private Label obeseBmiCategoryLabel;

    // BMR
    @FXML private Label bmrTitleLabel;
    @FXML private Label bmrDetailsLabel;

    // TDEE
    @FXML private Label tdeeTitleLabel;
    @FXML private Label tdeeDetailsLabel;
    @FXML private VBox tdeeResultView;
    @FXML private Label tdeeValueLabel;
    @FXML private VBox calculationSummaryGroup;
    @FXML private Label tdeeActivityLevelLabel;
    @FXML private Label tdeeBmrValueLabel;
    @FXML private VBox calorieTargetsGroup;
    @FXML private Label weightLossCaloriesLabel;
    @FXML private Label maintenanceCaloriesLabel;
    @FXML private Label weightGainCaloriesLabel;

    // Body Fat
    @FXML private VBox bodyFatResultView;
    @FXML private Label bodyFatValueLabel;
    @FXML private Label bodyFatCategoryLabel;
    @FXML private Label bodyFatMassLabel;
    @FXML private Label leanBodyMassLabel;
    @FXML private Label idealBodyFatLabel;
    @FXML private Label weightChangeTitleLabel;
    @FXML private Label weightChangeLabel;
    @FXML private Label essentialBodyFatLabel;
    @FXML private Label athletesBodyFatLabel;
    @FXML private Label fitnessBodyFatLabel;
    @FXML private Label averageBodyFatLabel;
    @FXML private Label obeseBodyFatLabel;

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
        initializeResponsiveWidthLayout(rootLayout, NARROW_BREAKPOINT);

        // ToolTip delay after hovering
        setToolTipDelay();

        // Fields Messages
        restoreAgeHelper();
        restoreHeightHelper();
        restoreWeightHelper();
        restoreActivityLevelHelper();
        restoreBodyFatHelper();
        restoreNeckHelper();
        restoreWaistHelper();
        restoreHipHelper();

        // Listeners
        addListeners();
    }

    // ── Button Actions ─────────────────────────────────────────────────
    @FXML
    public void handleCalculate() {
        CalculatorType calculatorType = (CalculatorType) calculatorGroup.getSelectedToggle().getUserData();

        switch (calculatorType) {
            case BMI -> handleBMI();
            case TDEE -> handleTDEE();
            case BODY_FAT -> handleBodyFat();
        }
    }

    private void handleBMI() {
        if(!validateInputs()) {
            hideResults();
            return;
        }

        String height = heightField.getText().trim();
        String weight = weightField.getText().trim();

        double heightMeters = NumberUtils.parseDecimal(height) / 100.0;
        double weightKg = NumberUtils.parseDecimal(weight);

        setBmiResult(heightMeters, weightKg);
        showResult(CalculatorType.BMI);
    }

    private void handleTDEE() {
        if (!validateInputs()) {
            hideResults();
            return;
        }

        String age = ageField.getText().trim();
        Gender gender = (Gender) genderGroup.getSelectedToggle().getUserData();
        String height = heightField.getText().trim();
        String weight = weightField.getText().trim();
        EnergyMode activityLevel = activityLevelComboBox.getSelectionModel().getSelectedItem();
        String bodyFatText = bodyFatField.getText().trim();

        int ageInt = Integer.parseInt(age);
        double heightCm = NumberUtils.parseDecimal(height);
        double weightKg = NumberUtils.parseDecimal(weight);
        Double bodyFatPercentage =
                bodyFatFieldGroup.isVisible() && !bodyFatText.isEmpty()
                        ? NumberUtils.parseDecimal(bodyFatText)
                        : null;

        setTdeeResult(ageInt, gender, heightCm, weightKg, activityLevel, bodyFatPercentage);
        showResult(CalculatorType.TDEE);
    }

    private void handleBodyFat() {
        if(!validateInputs()) {
            hideResults();
            return;
        }

        String age = ageField.getText().trim();
        String height = heightField.getText().trim();
        String weight = weightField.getText().trim();
        String neck = neckField.getText().trim();
        String waist = waistField.getText().trim();
        String hip = hipField.getText().trim();
        Gender gender = (Gender) genderGroup.getSelectedToggle().getUserData();

        int ageInt = Integer.parseInt(age);
        double heightCm = NumberUtils.parseDecimal(height);
        double weightKg = NumberUtils.parseDecimal(weight);
        double neckCm = NumberUtils.parseDecimal(neck);
        double waistCm = NumberUtils.parseDecimal(waist);
        Double hipCm = (gender == Gender.FEMALE) ? NumberUtils.parseDecimal(hip) : null;

        setBodyFatResult(ageInt, gender, heightCm, weightKg, neckCm, waistCm, hipCm);
        showResult(CalculatorType.BODY_FAT);
    }

    @FXML
    private void handleBodyFatToggle() {
        boolean showField = !bodyFatFieldGroup.isVisible();

        setVisible(bodyFatFieldGroup, showField);

        bodyFatToggleLabel.setText(
                showField
                        ? "− Remove body fat percentage"
                        : "+ Add body fat percentage"
        );

        if (!showField) {
            restoreBodyFatHelper();
        }

        if (areRequiredFieldsFilled(CalculatorType.TDEE)) {
            handleCalculate();
        }
    }

    // ── Validate Helpers ─────────────────────────────────────────────────
    private boolean validateInputs() {
        CalculatorType calculatorType = (CalculatorType) calculatorGroup.getSelectedToggle().getUserData();

        return switch (calculatorType) {
            case BMI -> validateBmiInputs();
            case TDEE -> validateTdeeInputs();
            case BODY_FAT -> validateBodyFatInputs();
        };
    }

    private boolean validateBmiInputs() {
        String height = heightField.getText().trim();
        String weight = weightField.getText().trim();

        boolean valid = true;

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

        return valid;
    }

    private boolean validateTdeeInputs() {
        String age = ageField.getText().trim();
        String bodyFat = bodyFatField.getText().trim();
        EnergyMode activityLevel = activityLevelComboBox.getSelectionModel().getSelectedItem();

        // BMI fields also need to be valid
        boolean valid = validateBmiInputs();

        if (!FitnessInputValidator.isAgeValid(age)) {
            showAgeMessage();
            shake(ageField);
            valid = false;
        }

        if (bodyFatFieldGroup.isVisible()
                && !FitnessInputValidator.isBodyFatValid(bodyFat)) {
            showBodyFatMessage();
            shake(bodyFatField);
            valid = false;
        }

        if (activityLevel == null) {
            showActivityLevelMessage();
            shake(activityLevelComboBox);
            valid = false;
        }

        return valid;
    }

    private boolean validateBodyFatInputs() {
        String age = ageField.getText().trim();
        String neck = neckField.getText().trim();
        String waist = waistField.getText().trim();
        String hip = hipField.getText().trim();
        Gender gender = (Gender) genderGroup.getSelectedToggle().getUserData();

        boolean valid = validateBmiInputs();

        if (!FitnessInputValidator.isAgeValid(age)) {
            showAgeMessage();
            shake(ageField);
            valid = false;
        }

        boolean neckValid = FitnessInputValidator.isNeckValid(neck);
        boolean waistValid = FitnessInputValidator.isWaistValid(waist);

        if (!neckValid) {
            showNeckMessage(AppConstants.Messages.INVALID_NECK_MESSAGE);
            shake(neckField);
            valid = false;
        }

        if (!waistValid) {
            showWaistMessage();
            shake(waistField);
            valid = false;
        }

        if (gender == Gender.FEMALE
                && !FitnessInputValidator.isHipValid(hip)) {
            showHipMessage();
            shake(hipField);
            valid = false;
        }

        if (neckValid && waistValid) {
            double neckValue = NumberUtils.parseDecimal(neck);
            double waistValue = NumberUtils.parseDecimal(waist);

            if (!FitnessInputValidator.isNeckWaistRelationValid(neckValue, waistValue)) {
                showNeckMessage(AppConstants.Messages.INVALID_NECK_WAIST_RELATION_MESSAGE);
                shake(neckField);
                valid = false;
            }
        }

        return valid;
    }

    // ── Responsive Helpers ─────────────────────────────────────────────────
    @Override
    public void updateWidthLayout(boolean narrow) {
        if (Objects.equals(narrowLayout, narrow)) return;

        narrowLayout = narrow;

        rootLayout.pseudoClassStateChanged(NARROW, narrow);

        if (narrow) {
            wideContainer.getChildren().clear();
            narrowContainer.getChildren().setAll(formPanel, resultPanel);

            setVisible(wideContainer, false);

            setVisible(narrowContainer, true);
        } else {
            narrowContainer.getChildren().clear();
            wideContainer.getChildren().setAll(formPanel, resultPanel);

            setVisible(narrowContainer, false);

            setVisible(wideContainer, true);
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
        neckField.textProperty().addListener((obs, oldValue, newValue) -> restoreNeckHelper());
        waistField.textProperty().addListener((obs, oldValue, newValue) -> restoreWaistHelper());
        hipField.textProperty().addListener((obs, oldValue, newValue) -> restoreHipHelper());
        activityLevelComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            activityLevelComboBox.pseudoClassStateChanged(NO_SELECTION, newVal == null);
            restoreActivityLevelHelper();

            // Calculate TDEE on activity change
            if (areRequiredFieldsFilled(CalculatorType.TDEE)) {
                handleCalculate();
            }
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

            if (areRequiredFieldsFilled(calculatorType)) {
                handleCalculate();
            } else {
                hideResults();
            }
        });

        genderGroup.selectedToggleProperty().addListener(
                (obs, oldToggle, newToggle) -> {
                    if (newToggle == null) {
                        if (oldToggle != null) {
                            genderGroup.selectToggle(oldToggle);
                        }
                        return;
                    }

                    Gender gender = (Gender) newToggle.getUserData();
                    setVisible(hipGroup, gender == Gender.FEMALE);

                    // Calculate TDEE/Body Fat on gender change
                    CalculatorType calculatorType = (CalculatorType) calculatorGroup.getSelectedToggle().getUserData();
                    if (areRequiredFieldsFilled(calculatorType)) {
                        handleCalculate();
                    }
                }
        );
    }

    private void initializeCalculatorControls() {
        tabButtons = List.of(bmiTab, tdeeTab, bodyFatTab);

        bmiTab.setUserData(CalculatorType.BMI);
        tdeeTab.setUserData(CalculatorType.TDEE);
        bodyFatTab.setUserData(CalculatorType.BODY_FAT);
        calculatorGroup.selectToggle(bmiTab);

        maleButton.setUserData(Gender.MALE);
        femaleButton.setUserData(Gender.FEMALE);
        genderGroup.selectToggle(maleButton);

        activityLevelComboBox.getItems().setAll(EnergyMode.values());
        activityLevelComboBox.pseudoClassStateChanged(NO_SELECTION, activityLevelComboBox.getValue() == null);

        updateCalculatorForm(CalculatorType.BMI);
    }

    // ── Calculator Helpers ─────────────────────────────────────────────────
    private void updateCalculatorForm(CalculatorType calculatorType) {
        boolean isTdee = calculatorType == CalculatorType.TDEE;
        boolean isBodyFat = calculatorType == CalculatorType.BODY_FAT;

        setVisible(ageGroup, isTdee || isBodyFat);
        setVisible(genderGroupContainer, isTdee || isBodyFat);

        setVisible(bodyFatSettingsGroup, isTdee);
        setVisible(activityLevelGroup, isTdee);
        setVisible(bodyFatInputFields, isBodyFat);
    }

    private void showResult(CalculatorType calculatorType) {
        boolean showBmi = calculatorType == CalculatorType.BMI;
        boolean showTdee = calculatorType == CalculatorType.TDEE;
        boolean showBodyFat = calculatorType == CalculatorType.BODY_FAT;

        setVisible(bmiResultView, showBmi);
        setVisible(tdeeResultView, showTdee);
        setVisible(bodyFatResultView, showBodyFat);

        resultPanel.setVisible(true);
    }

    // ── BMI Helpers ─────────────────────────────────────────────────
    private void setBmiResult(double heightMeters, double weightKg) {
        setBmiCategories();

        double bmi = CalculationService.calculateBmi(heightMeters, weightKg);
        bmiValueLabel.setText(NumberUtils.format("%.1f", bmi));

        setBmiStatus(bmi);

        setVisible(bmiStatusLabel, true);

        double minHealthyWeight = CalculationService.calculateHealthyWeightMin(heightMeters);
        double maxHealthyWeight = CalculationService.calculateHealthyWeightMax(heightMeters);
        healthyWeightRangeLabel.setText(NumberUtils.format("%.1f kg – %.1f kg", minHealthyWeight, maxHealthyWeight));

        double bmiPrime = CalculationService.calculateBmiPrime(bmi);
        bmiPrimeLabel.setText(NumberUtils.format("%.1f", bmiPrime));

        double ponderalIndex = CalculationService.calculatePonderalIndex(heightMeters, weightKg);
        ponderalIndexLabel.setText(NumberUtils.format("%.1f kg/m³", ponderalIndex));
    }

    private void setBmiCategories() {
        String underweight = NumberUtils.format("Below %.1f", 18.5);
        String normal = NumberUtils.formatRange(18.5, 24.9);
        String overweight = NumberUtils.formatRange(25.0, 29.9);
        String obese = NumberUtils.format("%.1f and above", 30.0);

        underweightBmiCategoryLabel.setText(underweight);
        normalBmiCategoryLabel.setText(normal);
        overweightBmiCategoryLabel.setText(overweight);
        obeseBmiCategoryLabel.setText(obese);
    }

    private void setBmiStatus(double bmi) {
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
    private void showBmrResult() {
        // Hide TDEE
        setVisible(tdeeTitleLabel, false);
        setVisible(tdeeDetailsLabel, false);

        setVisible(calculationSummaryGroup, false);

        setVisible(calorieTargetsGroup, false);

        // Show BMR
        setVisible(bmrTitleLabel, true);
        setVisible(bmrDetailsLabel, true);
    }

    // ── TDEE Helpers ─────────────────────────────────────────────────
    private void setTdeeResult(int age, Gender gender, double heightCm, double weightKg, EnergyMode mode, Double bodyFatPercentage) {

        double bmr = CalculationService.calculateBmr(age, gender, heightCm, weightKg, bodyFatPercentage);
        int bmrRounded = (int) Math.round(bmr);

        if (mode.isBmr()) {
            showBmrResult();

            tdeeValueLabel.setText(String.valueOf(bmrRounded));
            tdeeActivityLevelLabel.setText("BMR");
            tdeeBmrValueLabel.setText(String.format("%d kcal/day", bmrRounded));

            return;
        }

        showTdeeResult();

        double tdee = CalculationService.calculateTdee(bmr, mode.getActivityLevel());
        int tdeeRounded = (int) Math.round(tdee);

        int weightLossCalories = CalculationService.calculateWeightLossCalories(tdeeRounded);
        int weightGainCalories = CalculationService.calculateWeightGainCalories(tdeeRounded);

        tdeeValueLabel.setText(String.valueOf(tdeeRounded));
        tdeeActivityLevelLabel.setText(mode.getShortName());
        tdeeBmrValueLabel.setText(String.format("%d kcal/day", bmrRounded));
        weightLossCaloriesLabel.setText(String.format("%d kcal/day", weightLossCalories));
        maintenanceCaloriesLabel.setText(String.format("%d kcal/day", tdeeRounded));
        weightGainCaloriesLabel.setText(String.format("%d kcal/day", weightGainCalories));
    }

    private void showTdeeResult() {
        // Hide BMR
        setVisible(bmrTitleLabel, false);
        setVisible(bmrDetailsLabel, false);

        // Show TDEE
        setVisible(tdeeTitleLabel, true);
        setVisible(tdeeDetailsLabel, true);

        setVisible(calculationSummaryGroup, true);

        setVisible(calorieTargetsGroup, true);
    }

    // ── Body Fat Helpers ─────────────────────────────────────────────────
    private void setBodyFatResult(int age, Gender gender, double heightCm, double weightKg, double neckCm, double waistCm, Double hipCm) {

        double bodyFat = CalculationService.calculateBodyFatPercentage(gender, heightCm, neckCm, waistCm, hipCm);

        double displayedBodyFat = Math.round(bodyFat * 10.0) / 10.0;

        bodyFatValueLabel.setText(NumberUtils.format("%.1f", displayedBodyFat));

        // Set Body Fat field in TDEE Calculator
        bodyFatField.setText(NumberUtils.format("%.1f", displayedBodyFat));

        setBodyFatCategory(gender, displayedBodyFat);
        updateBodyFatCategoryRanges(gender);

        double fatMassKg = CalculationService.calculateFatMass(bodyFat, weightKg);
        bodyFatMassLabel.setText(NumberUtils.format("%.1f kg", fatMassKg));

        double leanMassKg = CalculationService.calculateLeanMass(weightKg, fatMassKg);
        leanBodyMassLabel.setText(NumberUtils.format("%.1f kg", leanMassKg));

        double idealBodyFat = CalculationService.calculateIdealBodyFat(age, gender);
        idealBodyFatLabel.setText(NumberUtils.format("%.1f %%", idealBodyFat));

        double bodyFatChange = CalculationService.calculateBodyFatChange(weightKg, bodyFat, idealBodyFat);
        if (bodyFatChange > 0) {
            weightChangeTitleLabel.setText("Weight to Lose");
            weightChangeLabel.setText(NumberUtils.format("%.1f kg", bodyFatChange));
        } else if (bodyFatChange < 0) {
            weightChangeTitleLabel.setText("Weight to Gain");
            weightChangeLabel.setText(NumberUtils.format("%.1f kg", Math.abs(bodyFatChange)));
        } else {
            weightChangeTitleLabel.setText("Weight Change");
            weightChangeLabel.setText("0.0 kg");
        }
    }

    private void setBodyFatCategory(Gender gender, double bodyFat) {
        bodyFatCategoryLabel.getStyleClass().removeAll(
                "body-fat-category-essential",
                "body-fat-category-athletes",
                "body-fat-category-fitness",
                "body-fat-category-average",
                "body-fat-category-obese"
        );

        double essentialMax;
        double athletesMax;
        double fitnessMax;
        double averageMax;
        double minimumEssential;

        if (gender == Gender.MALE) {
            minimumEssential = 2.0;
            essentialMax = 5.0;
            athletesMax = 13.0;
            fitnessMax = 17.0;
            averageMax = 25.0;
        }
        else {
            minimumEssential = 10.0;
            essentialMax = 13.0;
            athletesMax = 20.0;
            fitnessMax = 24.0;
            averageMax = 32.0;
        }

        if (bodyFat < minimumEssential) {
            bodyFatCategoryLabel.setText("Below Essential fat");
            bodyFatCategoryLabel.getStyleClass().add("body-fat-category-essential");
        } else if (bodyFat <= essentialMax) {
            bodyFatCategoryLabel.setText("Essential fat");
            bodyFatCategoryLabel.getStyleClass().add("body-fat-category-essential");
        } else if (bodyFat <= athletesMax) {
            bodyFatCategoryLabel.setText("Athletes");
            bodyFatCategoryLabel.getStyleClass().add("body-fat-category-athletes");
        } else if (bodyFat <= fitnessMax) {
            bodyFatCategoryLabel.setText("Fitness");
            bodyFatCategoryLabel.getStyleClass().add("body-fat-category-fitness");
        } else if (bodyFat <= averageMax) {
            bodyFatCategoryLabel.setText("Average");
            bodyFatCategoryLabel.getStyleClass().add("body-fat-category-average");
        } else {
            bodyFatCategoryLabel.setText("Obese");
            bodyFatCategoryLabel.getStyleClass().add("body-fat-category-obese");
        }

        setVisible(bodyFatCategoryLabel, true);
    }

    private void updateBodyFatCategoryRanges(Gender gender) {
        String essential;
        String athletes;
        String fitness;
        String average;
        String obese;

        if (gender == Gender.MALE) {
            essential = NumberUtils.formatRange(2.0, 5.0) + "%";
            athletes = NumberUtils.formatRange(5.1, 13.0) + "%";
            fitness = NumberUtils.formatRange(13.1, 17.0) + "%";
            average = NumberUtils.formatRange(17.1, 25.0) + "%";
            obese = NumberUtils.format("%.1f", 25.1) + "% and above";
        }
        else {
            essential = NumberUtils.formatRange(10.1, 13.0) + "%";
            athletes = NumberUtils.formatRange(13.1, 20.0) + "%";
            fitness = NumberUtils.formatRange(20.1, 24.0) + "%";
            average = NumberUtils.formatRange(24.1, 32.0) + "%";
            obese = NumberUtils.format("%.1f", 32.1) + "% and above";
        }

        essentialBodyFatLabel.setText(essential);
        athletesBodyFatLabel.setText(athletes);
        fitnessBodyFatLabel.setText(fitness);
        averageBodyFatLabel.setText(average);
        obeseBodyFatLabel.setText(obese);
    }

    // ── Result Helpers ─────────────────────────────────────────────────
    private void hideResults() {
        setVisible(bmiResultView, false);
        setVisible(tdeeResultView, false);
        setVisible(bodyFatResultView, false);

        resultPanel.setVisible(false);
    }

    private boolean areRequiredFieldsFilled(CalculatorType calculatorType) {
        if (calculatorType == CalculatorType.BMI) {
            return !heightField.getText().isBlank()
                    && !weightField.getText().isBlank();
        }

        if (calculatorType == CalculatorType.TDEE) {
            if (ageField.getText().isBlank()
                    || heightField.getText().isBlank()
                    || weightField.getText().isBlank()
                    || activityLevelComboBox.getValue() == null) {
                return false;
            }

            return !bodyFatFieldGroup.isVisible()
                    || !bodyFatField.getText().isBlank();
        }

        if (calculatorType == CalculatorType.BODY_FAT) {
            Gender gender = (Gender) genderGroup.getSelectedToggle().getUserData();

            if (ageField.getText().isBlank()
                || heightField.getText().isBlank()
                || weightField.getText().isBlank()
                || neckField.getText().isBlank()
                || waistField.getText().isBlank()) {
                return false;
            }

            return gender != Gender.FEMALE
                    || !hipField.getText().isBlank();
        }

        return true;
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

    // ── Body Fat Field Helpers ─────────────────────────────────────────────────
    private void showBodyFatMessage() {
        setFieldMessage(bodyFatMessage, AppConstants.Messages.INVALID_BODY_FAT_MESSAGE, true, bodyFatField);
    }

    private void restoreBodyFatHelper() {
        setFieldMessage(bodyFatMessage, AppConstants.Messages.HELPER_BODY_FAT_MESSAGE, false, bodyFatField);
    }

    // ── Neck Helpers ─────────────────────────────────────────────────
    private void showNeckMessage(String message) {
        setFieldMessage(neckMessage, message, true, neckField);
    }

    private void restoreNeckHelper() {
        setFieldMessage(neckMessage, AppConstants.Messages.HELPER_NECK_MESSAGE, false, neckField);
    }

    // ── Waist Helpers ─────────────────────────────────────────────────
    private void showWaistMessage() {
        setFieldMessage(waistMessage, AppConstants.Messages.INVALID_WAIST_MESSAGE, true, waistField);
    }

    private void restoreWaistHelper() {
        setFieldMessage(waistMessage, AppConstants.Messages.HELPER_WAIST_MESSAGE, false, waistField);
    }

    // ── Hip Helpers ─────────────────────────────────────────────────
    private void showHipMessage() {
        setFieldMessage(hipMessage, AppConstants.Messages.INVALID_HIP_MESSAGE, true, hipField);
    }

    private void restoreHipHelper() {
        setFieldMessage(hipMessage, AppConstants.Messages.HELPER_HIP_MESSAGE, false, hipField);
    }
}