package com.fittrack.controller.profile;

import com.fittrack.util.NumberUtils;
import javafx.scene.control.*;
import com.fittrack.api.profile.ProfileSetupApi;
import com.fittrack.controller.common.FormController;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.dto.profile.ProfileSetupRequest;
import com.fittrack.model.profile.ActivityLevel;
import com.fittrack.model.profile.Gender;
import com.fittrack.model.profile.WeightGoal;
import com.fittrack.session.UserSession;
import com.fittrack.config.AppConstants;
import com.fittrack.async.AsyncTaskRunner;
import com.fittrack.validation.FitnessInputValidator;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.ResourceBundle;

import static com.fittrack.model.profile.WeightGoal.*;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class ProfileSetupController extends FormController implements Initializable, ResponsiveLayout {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(ProfileSetupController.class);

    @Override
    protected Logger getLogger() { return log; }

    // Root and Scroll
    @FXML private StackPane rootLayout;
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

    // Constants
    private static final DateTimeFormatter DATE_OF_BIRTH_INPUT_FORMATTER = DateTimeFormatter.ofPattern("d.M.uuuu");
    private static final DateTimeFormatter DATE_OF_BIRTH_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.uuuu");

    // Adding PseudoClass to ComboBox (Change text color when nothing is selected)
    private static final PseudoClass NO_SELECTION = PseudoClass.getPseudoClass("no-selection");

    // Responsive breakpoint
    private static final int NARROW_BREAKPOINT = 460;

    // PseudoClass for Narrow screen size
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");

    // Is Narrow
    private Boolean narrowLayout;

    // API
    private final ProfileSetupApi profileSetupApi = new ProfileSetupApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Setup ScrollPane
        setupScrollContent.minHeightProperty().bind(
                setupScroll.viewportBoundsProperty().map(Bounds::getHeight)
        );

        // Initialize all form controls
        initializeProfileSetupControls();

        // Responsive initialize
        initializeResponsiveWidthLayout(rootLayout, NARROW_BREAKPOINT);

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

        // Listeners
        addListeners();
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

        // Hide Step 1 page
        setVisible(personalInfoStep, false);

        // Show Step 2 page
        setVisible(fitnessGoalsStep, true);
    }

    @FXML
    public void handleBack() {
        // Hide Step 2 page
        setVisible(fitnessGoalsStep, false);

        // Show Step 1 page
        setVisible(personalInfoStep, true);
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
        && FitnessInputValidator.isWeightValid(weight)
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

        finishButton.setDisable(true);
        finishButton.setText("Saving...");

        int userId = UserSession.getInstance().requireCurrentUser().id();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        LocalDate dateOfBirth = dateOfBirthPicker.getValue();
        Gender gender = (Gender) genderGroup.getSelectedToggle().getUserData();
        double heightValue = NumberUtils.parseDecimal(height);
        double weightValue = NumberUtils.parseDecimal(weight);
        Double goalWeightValue;
        Double weeklyGoalValue = weeklyGoal;

        if (goalType == MAINTAIN_WEIGHT) {
            goalWeightValue = null;
            weeklyGoalValue = null;
        } else {
            goalWeightValue = goalWeight.isBlank() ? null : NumberUtils.parseDecimal(goalWeight);
        }

        ProfileSetupRequest profileSetupRequest = new ProfileSetupRequest(
                userId,
                firstName,
                lastName,
                dateOfBirth,
                gender.name(),
                heightValue,
                activityLevel.name(),
                goalType.name(),
                goalWeightValue,
                weeklyGoalValue,
                weightValue
        );

        AsyncTaskRunner.run(
            () -> {
                profileSetupApi.completeProfile(profileSetupRequest);
                return null;
            },

            ignored -> {
                log.info("Profile setup completed for user ID: {}", userId);
                navigateTo(AppConstants.Views.MAIN_LAYOUT);
            },

            exception -> {
                log.error("Failed to complete profile setup.", exception);

                finishButton.setDisable(false);
                finishButton.setText("Finish");
            }
        );
    }

    // ── Initialize Helpers ─────────────────────────────────────────────────
    private void addListeners() {
        firstNameField.textProperty().addListener((obs, oldValue, newValue) -> restoreFirstNameHelper());
        lastNameField.textProperty().addListener((obs, oldValue, newValue) -> restoreLastNameHelper());
        dateOfBirthPicker.getEditor().textProperty().addListener((obs, oldValue, newValue) -> restoreDateOfBirthHelper());
        genderGroup.selectedToggleProperty().addListener(
                (obs, oldToggle, newToggle) -> {
                    if (newToggle == null && oldToggle != null) {
                        genderGroup.selectToggle(oldToggle);
                    }
                }
        );

        heightField.textProperty().addListener((obs, oldValue, newValue) -> restoreHeightHelper());
        weightField.textProperty().addListener((obs, oldValue, newValue) -> restoreWeightHelper());
        activityLevelComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            activityLevelComboBox.pseudoClassStateChanged(NO_SELECTION, newValue == null);
            restoreActivityLevelHelper();
        });
        goalTypeComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            goalTypeComboBox.pseudoClassStateChanged(NO_SELECTION, newValue == null);
            restoreGoalTypeHelper();
            updateGoalWeightBoxVisibility();
        });
        goalWeightField.textProperty().addListener((obs, oldValue, newValue) -> restoreGoalWeightHelper());
        weeklyGoalComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            weeklyGoalComboBox.pseudoClassStateChanged(NO_SELECTION, newValue == null);
            restoreWeeklyGoalHelper();
        });
    }

    private void initializeProfileSetupControls() {
        // DatePicker initialize
        LocalDate latestAllowedDateOfBirth = LocalDate.now().minusYears(AppConstants.Validation.MIN_AGE);

        dateOfBirthPicker.setDayCellFactory(picker -> new DateCell() {

            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                setDisable(
                        empty || date.isAfter(latestAllowedDateOfBirth)
                );
            }
        });

        dateOfBirthPicker.setConverter(new StringConverter<>() {

            @Override
            public String toString(LocalDate date) {
                if (date == null) {
                    return "";
                }

                return date.format(DATE_OF_BIRTH_DISPLAY_FORMATTER);
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) {
                    return dateOfBirthPicker.getValue();
                }

                try {
                    return LocalDate.parse(
                            text.trim(),
                            DATE_OF_BIRTH_INPUT_FORMATTER
                    );
                } catch (DateTimeParseException _) {
                    return dateOfBirthPicker.getValue();
                }
            }
        });
        dateOfBirthPicker.setValue(null);
        dateOfBirthPicker.setShowWeekNumbers(false);

        // Gender ToggleButton value initialize
        maleButton.setUserData(Gender.MALE);
        femaleButton.setUserData(Gender.FEMALE);
        genderGroup.selectToggle(maleButton);

        // ComboBox fill and PseudoClass add
        activityLevelComboBox.getItems().setAll(ActivityLevel.values());
        activityLevelComboBox.pseudoClassStateChanged(NO_SELECTION, activityLevelComboBox.getValue() == null);
        goalTypeComboBox.getItems().setAll(WeightGoal.values());
        goalTypeComboBox.pseudoClassStateChanged(NO_SELECTION, goalTypeComboBox.getValue() == null);
        weeklyGoalComboBox.getItems().setAll(0.25, 0.5, 0.75, 1.0);
        weeklyGoalComboBox.pseudoClassStateChanged(NO_SELECTION, weeklyGoalComboBox.getValue() == null);
    }

    // ── Responsive Helpers ─────────────────────────────────────────────────
    @Override
    public void updateWidthLayout(boolean narrow) {
        if (Objects.equals(narrowLayout, narrow)) return;

        narrowLayout = narrow;

        rootLayout.pseudoClassStateChanged(NARROW, narrow);

        backButton.setText(narrow ? "←" : "← Back");
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
        String enteredDate = dateOfBirthPicker.getEditor().getText().trim();

        if (enteredDate.isEmpty()) {
            showDateOfBirthMessage(AppConstants.Messages.INVALID_DATE_OF_BIRTH_FORMAT_MESSAGE);
            return false;
        }

        LocalDate dateOfBirth;

        try {
            dateOfBirth = LocalDate.parse(
                    enteredDate,
                    DATE_OF_BIRTH_INPUT_FORMATTER
            );
        } catch (DateTimeParseException _) {
            showDateOfBirthMessage(AppConstants.Messages.INVALID_DATE_OF_BIRTH_FORMAT_MESSAGE);
            return false;
        }

        LocalDate latestAllowedDateOfBirth = LocalDate.now().minusYears(AppConstants.Validation.MIN_AGE);

        if (dateOfBirth.isAfter(latestAllowedDateOfBirth)) {
            showDateOfBirthMessage(AppConstants.Messages.INVALID_DATE_OF_BIRTH_AGE_MESSAGE);
            return false;
        }

        int age = Period.between(
                dateOfBirth,
                LocalDate.now()
        ).getYears();

        if (!FitnessInputValidator.isAgeValid(age)) {
            showDateOfBirthMessage(AppConstants.Messages.INVALID_DATE_OF_BIRTH_AGE_MESSAGE);
            return false;
        }

        dateOfBirthPicker.setValue(dateOfBirth);
        dateOfBirthPicker.getEditor().setText(
                dateOfBirth.format(DATE_OF_BIRTH_DISPLAY_FORMATTER)
        );

        restoreDateOfBirthHelper();
        return true;
    }

    private void showDateOfBirthMessage(String message) {
        setFieldMessage(dateOfBirthMessage, message, true, dateOfBirthPicker);
    }

    private void restoreDateOfBirthHelper() {
        setFieldMessage(dateOfBirthMessage, AppConstants.Messages.HELPER_DATE_OF_BIRTH_MESSAGE, false, dateOfBirthPicker);
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

    // ── Goal type Helpers ─────────────────────────────────────────────────
    private void showGoalTypeMessage() {
        setFieldMessage(goalTypeMessage, AppConstants.Messages.GOAL_NOT_SELECTED_MESSAGE, true, goalTypeComboBox);
    }

    private void restoreGoalTypeHelper() {
        setFieldMessage(goalTypeMessage, AppConstants.Messages.HELPER_GOAL_TYPE_MESSAGE, false, goalTypeComboBox);
    }

    private void updateGoalWeightBoxVisibility() {
        WeightGoal selectedGoal = goalTypeComboBox.getSelectionModel().getSelectedItem();
        setVisible(goalWeightBox, selectedGoal != MAINTAIN_WEIGHT && selectedGoal != null);
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

        if (!FitnessInputValidator.isWeightValid(goalWeightText)) return false;

        double goalWeight = NumberUtils.parseDecimal(goalWeightText);
        double currentWeight = NumberUtils.parseDecimal(currentWeightText);

        return switch (goalType) {
            case LOSE_WEIGHT -> goalWeight < currentWeight;
            case GAIN_WEIGHT -> goalWeight > currentWeight;
            default -> true;
        };
    }
}