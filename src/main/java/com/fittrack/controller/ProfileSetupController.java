package com.fittrack.controller;

import com.fittrack.controller.Login_Register.FormController;
import com.fittrack.util.AppConstants;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class ProfileSetupController extends FormController implements Initializable {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(ProfileSetupController.class);

    @Override
    protected Logger getLogger() { return log; }

    @FXML private StackPane rootPane;

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
    @FXML private Button backButton;

    @FXML private VBox personalInfoStep;
    @FXML private VBox fitnessGoalsStep;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        restoreFirstNameHelper();
        restoreLastNameHelper();
        restoreDateOfBirthHelper();

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

        // Get selected gender
        //String gender = genderGroup.getSelectedToggle().getUserData().toString();
    }

    @FXML
    public void handleNext() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();

        boolean valid = true;

        if(!isValidName(firstName)) {
            showFirstNameMessage();
            shake(firstNameField);
            valid = false;
        }

        if(!isValidName(lastName)) {
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

    // ── First name Helpers ─────────────────────────────────────────────────
    private boolean isValidName(String name) {
        return name.trim().length() >= AppConstants.Validation.MIN_NAME_LENGTH
                && name.length() <= AppConstants.Validation.MAX_NAME_LENGTH
                && name.matches("^\\p{L}[\\p{L} '\\-]*\\p{L}$");
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
}