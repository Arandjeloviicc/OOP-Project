package com.fittrack.controller.profile.editor;

import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.FormController;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.dto.profile.editor.PersonalInfoUpdateRequest;
import com.fittrack.model.profile.Gender;
import com.fittrack.model.profile.ProfileData;
import com.fittrack.ui.form.DateOfBirthPickerConfigurer;
import com.fittrack.ui.form.GenderToggleConfigurer;
import com.fittrack.ui.scene.SceneShortcuts;
import com.fittrack.util.NumberUtils;
import com.fittrack.validation.FitnessInputValidator;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ProfilePersonalInfoEditorController extends FormController implements Initializable, ResponsiveLayout {

    // Root
    @FXML private StackPane rootLayout;
    @FXML private VBox dialogContainer;
    @FXML private GridPane formGrid;

    // Groups
    @FXML private VBox firstNameGroup;
    @FXML private VBox lastNameGroup;
    @FXML private VBox dateOfBirthGroup;
    @FXML private VBox genderGroupContainer;
    @FXML private VBox heightGroup;

    // Fields
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private ToggleGroup genderGroup;
    @FXML private ToggleButton maleButton;
    @FXML private ToggleButton femaleButton;
    @FXML private TextField heightField;

    // Messages
    @FXML private Label firstNameMessage;
    @FXML private Label lastNameMessage;
    @FXML private Label dateOfBirthMessage;
    @FXML private Label heightMessage;

    // Buttons
    @FXML private Button saveButton;

    // Actions
    private Runnable onCancelAction;
    private Consumer<PersonalInfoUpdateRequest> onSaveAction;

    // Change alignment to TOP_CENTER
    private boolean narrowLayout;
    private Consumer<Boolean> onNarrowLayoutChanged;

    // Responsive
    private static final int NARROW_BREAKPOINT = 440;
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");

    // ── Initialization ─────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize controls and listeners
        initializeControls();
        addListeners();

        setWideLayout();

        // Keyboard Shortcuts
        SceneShortcuts.forNode(rootLayout)
                .onEscape(this::handleCancel)
                .onEnter(this::handleSave);

    }

    private void initializeControls() {
        GenderToggleConfigurer.configure(genderGroup, maleButton, femaleButton);
        DateOfBirthPickerConfigurer.configure(dateOfBirthPicker);
    }

    public void initializeResponsiveLayout(Region observedRegion) {
        initializeResponsiveWidthLayout(observedRegion, NARROW_BREAKPOINT);
    }

    private void addListeners() {
        firstNameField.textProperty().addListener((obs, oldValue, newValue) -> clearFirstNameError());
        lastNameField.textProperty().addListener((obs, oldValue, newValue) -> clearLastNameError());
        dateOfBirthPicker.getEditor().textProperty().addListener((obs, oldValue, newValue) -> clearDateOfBirthError());
        heightField.textProperty().addListener((obs, oldValue, newValue) -> clearHeightError());
    }

    // ── Configuration ─────────────────────────────────────────────────
    public void setData(ProfileData profile) {
        firstNameField.setText(profile.firstName());
        lastNameField.setText(profile.lastName());
        dateOfBirthPicker.setValue(profile.dateOfBirth());
        heightField.setText(NumberUtils.formatInputDecimal(profile.height()));

        if (profile.gender() == Gender.MALE) {
            maleButton.setSelected(true);
        } else if (profile.gender() == Gender.FEMALE) {
            femaleButton.setSelected(true);
        }
    }

    public void setOnCancelAction(Runnable onCancelAction) {
        this.onCancelAction = onCancelAction;
    }

    public void setOnSaveAction(Consumer<PersonalInfoUpdateRequest> onSaveAction) {
        this.onSaveAction = onSaveAction;
    }

    public void setOnNarrowLayoutChanged(Consumer<Boolean> onNarrowLayoutChanged) {
        this.onNarrowLayoutChanged = onNarrowLayoutChanged;

        if (onNarrowLayoutChanged != null) {
            onNarrowLayoutChanged.accept(narrowLayout);
        }
    }

    // ── Responsive ─────────────────────────────────────────────────
    @Override
    public void updateWidthLayout(boolean narrow) {
        narrowLayout = narrow;

        rootLayout.pseudoClassStateChanged(NARROW, narrow);

        if (narrow) {
            setNarrowLayout();

            dialogContainer.prefWidthProperty()
                    .bind(rootLayout.widthProperty());
        } else {
            setWideLayout();

            dialogContainer.prefWidthProperty().unbind();
            dialogContainer.setPrefWidth(
                    Region.USE_COMPUTED_SIZE
            );
        }

        if (onNarrowLayoutChanged != null) {
            onNarrowLayoutChanged.accept(narrow);
        }
    }

    @Override
    public void updateHeightLayout(boolean shortLayout) {
        // TODO document why this method is empty
    }

    private void setWideLayout() {
        configureColumns(2);

        GridPane.setConstraints(firstNameGroup, 0, 0, 1, 1);
        GridPane.setConstraints(lastNameGroup, 1, 0, 1, 1);

        GridPane.setConstraints(dateOfBirthGroup, 0, 1, 2, 1);

        GridPane.setConstraints(genderGroupContainer, 0, 2, 1, 1);
        GridPane.setConstraints(heightGroup, 1, 2, 1, 1);
    }

    private void setNarrowLayout() {
        configureColumns(1);

        GridPane.setConstraints(firstNameGroup, 0, 0, 1, 1);
        GridPane.setConstraints(lastNameGroup, 0, 1, 1, 1);
        GridPane.setConstraints(dateOfBirthGroup, 0, 2, 1, 1);
        GridPane.setConstraints(genderGroupContainer, 0, 3, 1, 1);
        GridPane.setConstraints(heightGroup, 0, 4, 1, 1);
    }

    private void configureColumns(int count) {
        formGrid.getColumnConstraints().clear();

        for (int i = 0; i < count; i++) {
            ColumnConstraints column = new ColumnConstraints();

            column.setPercentWidth(100.0 / count);
            column.setHgrow(Priority.ALWAYS);
            column.setFillWidth(true);

            formGrid.getColumnConstraints().add(column);
        }
    }

    // ── Button Actions ─────────────────────────────────────────────────
    @FXML
    private void handleCancel() {
        if (isLoading(saveButton)) {
            return;
        }

        if (onCancelAction != null) {
            onCancelAction.run();
        }
    }

    @FXML
    private void handleSave() {
        if (isLoading(saveButton)) {
            return;
        }

        if (!isFormValid()) {
            return;
        }

        Gender gender = (Gender) genderGroup.getSelectedToggle().getUserData();

        PersonalInfoUpdateRequest request =
                new PersonalInfoUpdateRequest(
                        firstNameField.getText().trim(),
                        lastNameField.getText().trim(),
                        dateOfBirthPicker.getValue(),
                        gender.name(),
                        NumberUtils.parseDecimal(heightField.getText().trim())
                );

        if (onSaveAction != null) {
            onSaveAction.accept(request);
        }
    }

    private boolean isFormValid() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String height = heightField.getText().trim();

        boolean valid = true;

        if (!FitnessInputValidator.isNameValid(firstName)) {
            showFirstNameMessage();
            shake(firstNameField);
            valid = false;
        }

        if (!FitnessInputValidator.isNameValid(lastName)) {
            showLastNameMessage();
            shake(lastNameField);
            valid = false;
        }

        if (!validateDateOfBirth()) {
            shake(dateOfBirthPicker);
            valid = false;
        }

        if (!FitnessInputValidator.isHeightValid(height)) {
            showHeightMessage();
            shake(heightField);
            valid = false;
        }

        return valid;
    }

    // ── Save State ─────────────────────────────────────────────
    public void setSaving(boolean saving) {
        if (saving) {
            setLoading(saveButton, "Saving...");
        } else {
            resetLoading(saveButton);
        }
    }

    // ── First name Helpers ─────────────────────────────────────────────────
    private void showFirstNameMessage() {
        setFieldMessage(firstNameMessage, AppConstants.Messages.INVALID_FIRST_NAME_MESSAGE, true, firstNameField);
    }

    private void clearFirstNameError() {
        clearFieldMessage(firstNameMessage, firstNameField);
    }

    // ── Last name Helpers ─────────────────────────────────────────────────
    private void showLastNameMessage() {
        setFieldMessage(lastNameMessage, AppConstants.Messages.INVALID_LAST_NAME_MESSAGE, true, lastNameField);
    }

    private void clearLastNameError() {
        clearFieldMessage(lastNameMessage, lastNameField);
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
                    DateOfBirthPickerConfigurer.inputFormatter()
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
                dateOfBirth.format(DateOfBirthPickerConfigurer.displayFormatter())
        );

        clearDateOfBirthError();
        return true;
    }

    private void showDateOfBirthMessage(String message) {
        setFieldMessage(dateOfBirthMessage, message, true, dateOfBirthPicker);
    }

    private void clearDateOfBirthError() {
        clearFieldMessage(dateOfBirthMessage, dateOfBirthPicker);
    }

    // ── Height Helpers ─────────────────────────────────────────────────
    private void showHeightMessage() {
        setFieldMessage(heightMessage, AppConstants.Messages.INVALID_HEIGHT_MESSAGE, true, heightField);
    }

    private void clearHeightError() {
        clearFieldMessage(heightMessage, heightField);
    }
}