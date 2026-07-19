package com.fittrack.controller;

import com.fittrack.controller.Login_Register.FormController;
import com.fittrack.model.ActivityLevel;
import com.fittrack.model.WeightGoal;
import com.fittrack.util.AppConstants;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.util.ResourceBundle;

import static com.fittrack.model.WeightGoal.*;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class ProfileSetupController extends FormController implements Initializable {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(ProfileSetupController.class);

    @Override
    protected Logger getLogger() { return log; }

    // Root and Scroll
    @FXML private StackPane rootPane;
    @FXML private ScrollPane setupScroll;
    @FXML private StackPane setupScrollContent;

    // Personal info step
    @FXML private VBox personalInfoStep;
    @FXML private TextField firstNameField;
    @FXML private Label firstNameMessage;
    @FXML private TextField lastNameField;
    @FXML private Label lastNameMessage;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private Label dateOfBirthMessage;
    @FXML private ToggleGroup genderGroup;
    @FXML private ToggleButton maleButton;
    @FXML private ToggleButton femaleButton;
    @FXML private Button nextButton;

    // Fitness goals step
    @FXML private VBox goalWeightBox;
    @FXML private VBox fitnessGoalsStep;
    @FXML private Button backButton;
    @FXML private TextField heightField;
    @FXML private Label heightMessage;
    @FXML private TextField weightField;
    @FXML private Label weightMessage;
    @FXML private ComboBox<ActivityLevel> activityLevelComboBox;
    @FXML private Label activityLevelMessage;
    @FXML private ComboBox<WeightGoal> goalTypeComboBox;
    @FXML private Label goalTypeMessage;
    @FXML private TextField goalWeightField;
    @FXML private Label goalWeightMessage;
    @FXML private ComboBox<Double> weeklyGoalComboBox;
    @FXML private Label weeklyGoalMessage;
    @FXML private Button finishButton;

    // Adding PseudoClass to ComboBox (Change text color when nothing is selected)
    private static final PseudoClass NO_SELECTION = PseudoClass.getPseudoClass("no-selection");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Setup ScrollPane
        setupScrollContent.minHeightProperty().bind(
                setupScroll.viewportBoundsProperty().map(Bounds::getHeight)
        );

        // Initialize input messages
        restoreFirstNameHelper();
        restoreLastNameHelper();
        restoreDateOfBirthHelper();

        restoreHeightHelper();
        restoreWeightHelper();
        restoreActivityLevelHelper();
        restoreGoalTypeHelper();
        restoreGoalWeightHelper();
        restoreWeeklyGoalHelper();

        // DatePicker initialize
        dateOfBirthPicker.setDayCellFactory(picker -> new DateCell() {

            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                setDisable(
                        empty || date.isAfter(LocalDate.now())
                );
            }
        });
        dateOfBirthPicker.setValue(null);
        dateOfBirthPicker.setShowWeekNumbers(false);
        // Opens date menu on click, not just on date icon
        dateOfBirthPicker.getEditor().setOnMouseClicked(event -> dateOfBirthPicker.show());
        // Disable manual date input
        dateOfBirthPicker.getEditor().setEditable(false);
        dateOfBirthPicker.getEditor().addEventFilter(KeyEvent.ANY, KeyEvent::consume);

        // ComboBox fill and PseudoClass add
        activityLevelComboBox.getItems().setAll(ActivityLevel.values());
        activityLevelComboBox.pseudoClassStateChanged(NO_SELECTION, activityLevelComboBox.getValue() == null);
        goalTypeComboBox.getItems().setAll(WeightGoal.values());
        goalTypeComboBox.pseudoClassStateChanged(NO_SELECTION, goalTypeComboBox.getValue() == null);
        weeklyGoalComboBox.getItems().setAll(0.25, 0.5, 0.75, 1.0);
        weeklyGoalComboBox.pseudoClassStateChanged(NO_SELECTION, weeklyGoalComboBox.getValue() == null);

        // Listeners
        firstNameField.textProperty().addListener((obs, oldValue, newValue) -> restoreFirstNameHelper());
        lastNameField.textProperty().addListener((obs, oldValue, newValue) -> restoreLastNameHelper());
        dateOfBirthPicker.valueProperty().addListener((obs, oldValue, newValue) -> restoreDateOfBirthHelper());
        genderGroup.selectedToggleProperty().addListener(
                (obs, oldToggle, newToggle) -> {
                    if (newToggle == null && oldToggle != null) {
                        genderGroup.selectToggle(oldToggle);
                    }
                }
        );

        heightField.textProperty().addListener((obs, oldValue, newValue) -> restoreHeightHelper());
        weightField.textProperty().addListener((obs, oldValue, newValue) -> restoreWeightHelper());
        activityLevelComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            activityLevelComboBox.pseudoClassStateChanged(NO_SELECTION, newVal == null);
            restoreActivityLevelHelper();
        });
        goalTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            goalTypeComboBox.pseudoClassStateChanged(NO_SELECTION, newVal == null);
            restoreGoalTypeHelper();
            updateGoalWeightBoxVisibility();
        });
        goalWeightField.textProperty().addListener((obs, oldValue, newValue) -> restoreGoalWeightHelper());
        weeklyGoalComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            weeklyGoalComboBox.pseudoClassStateChanged(NO_SELECTION, newVal == null);
            restoreWeeklyGoalHelper();
        });

        // Get selected gender
        //String gender = genderGroup.getSelectedToggle().getUserData().toString();
    }

    @FXML
    public void handleNext() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();

        boolean valid = true;

        if(!isNameValid(firstName)) {
            showFirstNameMessage();
            shake(firstNameField);
            valid = false;
        }

        if(!isNameValid(lastName)) {
            showLastNameMessage();
            shake(lastNameField);
            valid = false;
        }

        if(!validateDateOfBirth()) {
            shake(dateOfBirthPicker);
            valid = false;
        }

        if(!valid) return;

        personalInfoStep.setVisible(false);
        personalInfoStep.setManaged(false);

        fitnessGoalsStep.setVisible(true);
        fitnessGoalsStep.setManaged(true);
    }

    @FXML
    public void handleBack() {
        fitnessGoalsStep.setVisible(false);
        fitnessGoalsStep.setManaged(false);

        personalInfoStep.setVisible(true);
        personalInfoStep.setManaged(true);
    }

    @FXML
    public void handleFinish() {
        String height = heightField.getText().trim();
        String weight = weightField.getText().trim();
        ActivityLevel activityLevel = activityLevelComboBox.getSelectionModel().getSelectedItem();
        WeightGoal goalType = goalTypeComboBox.getSelectionModel().getSelectedItem();
        String goalWeight = goalWeightField.getText().trim();
        Double weeklyGoal = weeklyGoalComboBox.getSelectionModel().getSelectedItem();

        boolean valid = true;

        if(!isHeightValid(height)) {
            showHeightMessage();
            shake(heightField);
            valid = false;
        }

        if(!isWeightValid(weight)) {
            showWeightMessage();
            shake(weightField);
            valid = false;
        }

        if(activityLevel == null) {
            showActivityLevelMessage();
            shake(activityLevelComboBox);
            valid = false;
        }

        if(goalType == null) {
            showGoalTypeMessage();
            shake(goalTypeComboBox);
            valid = false;
        }

        if((goalType == LOSE_WEIGHT || goalType == GAIN_WEIGHT)
        && isWeightValid(weight)
        && !isGoalWeightValid(goalWeight, weight, goalType)) {

            String message = goalType == LOSE_WEIGHT
                    ? AppConstants.Messages.INVALID_GOAL_WEIGHT_LOSE_MESSAGE
                    : AppConstants.Messages.INVALID_GOAL_WEIGHT_GAIN_MESSAGE;

            showGoalWeightMessage(message);
            shake(goalWeightField);
            valid = false;
        }

        if ((goalType == LOSE_WEIGHT || goalType == GAIN_WEIGHT)
            && weeklyGoal == null) {
            showWeeklyGoalMessage();
            shake(weeklyGoalComboBox);
            valid = false;
        }

        if(!valid) return;

        log.info("Yes");
    }

    // ── First name Helpers ─────────────────────────────────────────────────
    private boolean isNameValid(String name) {
        return name.trim().length() >= AppConstants.Validation.MIN_NAME_LENGTH
                && name.trim().length() <= AppConstants.Validation.MAX_NAME_LENGTH
                && name.trim().matches("^\\p{L}[\\p{L} '\\-]*\\p{L}$");
    }

    private void showFirstNameMessage() {
        setFieldMessage(firstNameMessage, AppConstants.Messages.INVALID_FIRST_NAME_MESSAGE, true, firstNameField);
    }

    private void restoreFirstNameHelper() {
        setFieldMessage(firstNameMessage, AppConstants.Messages.HELPER_FIRST_NAME_MESSAGE, false, firstNameField);
    }

    // ── Last name Helpers ─────────────────────────────────────────────────
    private void showLastNameMessage() {
        setFieldMessage(lastNameMessage, AppConstants.Messages.INVALID_LAST_NAME_MESSAGE, true, lastNameField);
    }

    private void restoreLastNameHelper() {
        setFieldMessage(lastNameMessage, AppConstants.Messages.HELPER_LAST_NAME_MESSAGE, false, lastNameField);
    }

    // ── Date of birth Helpers ─────────────────────────────────────────────────
    private boolean validateDateOfBirth() {
        LocalDate dateOfBirth = dateOfBirthPicker.getValue();

        if(dateOfBirth == null) {
            showDateOfBirthMessage();
            return false;
        }

        LocalDate today = LocalDate.now();

        int age = Period.between(dateOfBirth, today).getYears();

        if(age < AppConstants.Validation.MIN_AGE
        || age > AppConstants.Validation.MAX_AGE) {
            showDateOfBirthMessage();
            return false;
        }

        restoreDateOfBirthHelper();
        return true;
    }

    private void showDateOfBirthMessage() {
        setFieldMessage(dateOfBirthMessage, AppConstants.Messages.INVALID_DATE_OF_BIRTH_MESSAGE, true, dateOfBirthPicker);
    }

    private void restoreDateOfBirthHelper() {
        setFieldMessage(dateOfBirthMessage, AppConstants.Messages.HELPER_DATE_OF_BIRTH_MESSAGE, false, dateOfBirthPicker);
    }

    // ── Height Helpers ─────────────────────────────────────────────────
    private boolean isHeightValid(String height) {
        if (height == null || !height.trim().matches("^\\d{1,3}(\\.\\d{1,2})?$")) {
            return false;
        }
        double value = Double.parseDouble(height.trim());
        return value >= AppConstants.Validation.MIN_HEIGHT && value <= AppConstants.Validation.MAX_HEIGHT;
    }

    private void showHeightMessage() {
        setFieldMessage(heightMessage, AppConstants.Messages.INVALID_HEIGHT_MESSAGE, true, heightField);
    }

    private void restoreHeightHelper() {
        setFieldMessage(heightMessage, AppConstants.Messages.HELPER_HEIGHT_MESSAGE, false, heightField);
    }

    // ── Weight Helpers ─────────────────────────────────────────────────
    private boolean isWeightValid(String weight) {
        if (weight == null || !weight.trim().matches("^\\d{1,3}(\\.\\d{1,2})?$")) {
            return false;
        }
        double value = Double.parseDouble(weight.trim());
        return value >= AppConstants.Validation.MIN_WEIGHT && value <= AppConstants.Validation.MAX_WEIGHT;
    }

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

    // ── Goal type Helpers ─────────────────────────────────────────────────
    private void showGoalTypeMessage() {
        setFieldMessage(goalTypeMessage, AppConstants.Messages.GOAL_NOT_SELECTED_MESSAGE, true, goalTypeComboBox);
    }

    private void restoreGoalTypeHelper() {
        setFieldMessage(goalTypeMessage, AppConstants.Messages.HELPER_GOAL_TYPE_MESSAGE, false, goalTypeComboBox);
    }

    private void updateGoalWeightBoxVisibility() {
        WeightGoal selectedGoal = goalTypeComboBox.getSelectionModel().getSelectedItem();
        if(selectedGoal == MAINTAIN_WEIGHT || selectedGoal == null) {
            goalWeightBox.setVisible(false);
            goalWeightBox.setManaged(false);
        }
        else {
            goalWeightBox.setVisible(true);
            goalWeightBox.setManaged(true);
        }
    }

    // ── Goal weight Helpers ─────────────────────────────────────────────────
    private void showGoalWeightMessage(String message) {
        setFieldMessage(goalWeightMessage, message, true, goalWeightField);
    }

    private void restoreGoalWeightHelper() {
        setFieldMessage(goalWeightMessage, AppConstants.Messages.HELPER_GOAL_WEIGHT_MESSAGE, false, goalWeightField);
    }

    // ── Weekly goal Helpers ─────────────────────────────────────────────────
    private void showWeeklyGoalMessage() {
        setFieldMessage(weeklyGoalMessage, AppConstants.Messages.WEEKLY_GOAL_NOT_SELECTED_MESSAGE, true, weeklyGoalComboBox);
    }

    private void restoreWeeklyGoalHelper() {
        setFieldMessage(weeklyGoalMessage, AppConstants.Messages.HELPER_WEEKLY_GOAL_MESSAGE, false, weeklyGoalComboBox);
    }

    private boolean isGoalWeightValid(String goalWeightText, String currentWeightText, WeightGoal goalType) {
        if (goalWeightText.isEmpty()) return true; // It's optional

        if (!isWeightValid(goalWeightText)) return false;

        double goalWeight = Double.parseDouble(goalWeightText);
        double currentWeight = Double.parseDouble(currentWeightText);

        return switch (goalType) {
            case LOSE_WEIGHT -> goalWeight < currentWeight;
            case GAIN_WEIGHT -> goalWeight > currentWeight;
            default -> true;
        };
    }
}