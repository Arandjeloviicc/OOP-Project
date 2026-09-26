package com.fittrack.controller.measurements.editor;

import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.FormController;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.dto.measurements.body.BodyMeasurementRequest;
import com.fittrack.dto.measurements.body.BodyMeasurementResponse;
import com.fittrack.model.profile.Gender;
import com.fittrack.ui.form.AppDatePickerConfigurer;
import com.fittrack.ui.scene.SceneShortcuts;
import com.fittrack.util.NumberUtils;
import com.fittrack.validation.FitnessInputValidator;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class BodyMeasurementEditorController extends FormController implements Initializable, ResponsiveLayout {

    @FXML private StackPane rootLayout;
    @FXML private Label titleLabel;

    @FXML private TextField neckField;
    @FXML private Label neckMessage;

    @FXML private TextField waistField;
    @FXML private Label waistMessage;

    @FXML private VBox hipGroup;
    @FXML private TextField hipField;
    @FXML private Label hipMessage;

    @FXML private DatePicker datePicker;
    @FXML private Label dateMessage;

    @FXML private Label actionMessage;
    @FXML private Button saveButton;

    // Data
    private Gender gender;

    // Actions
    private Runnable onCancelAction;
    private Consumer<BodyMeasurementRequest> onSaveAction;
    private Consumer<Boolean> onNarrowLayoutChanged;

    // Responsive
    private static final int NARROW_BREAKPOINT = 440;
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");

    // ── Initialization ─────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize controls and listeners
        initializeBodyMeasurementControls();
        addListeners();

        // Keyboard Shortcuts
        SceneShortcuts.forNode(rootLayout)
                .onEscape(this::handleCancel)
                .onEnter(this::handleSave);
    }

    private void initializeBodyMeasurementControls() {
        AppDatePickerConfigurer.configure(datePicker);
    }

    private void addListeners() {
        neckField.textProperty().addListener((observable, oldValue, newValue) -> clearNeckError());
        waistField.textProperty().addListener((observable, oldValue, newValue) -> clearWaistError());
        hipField.textProperty().addListener((observable, oldValue, newValue) -> clearHipError());
        datePicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> clearDateError());
    }

    public void initializeResponsiveLayout(Region widthSource) {
        initializeResponsiveWidthLayout(widthSource, NARROW_BREAKPOINT);
    }

    // ── Configuration ──────────────────────────────────────────────
    public void setCreateMode(Gender gender) {
        configureGender(gender);

        titleLabel.setText("Add body measurement");
        saveButton.setText("Add");

        neckField.clear();
        waistField.clear();
        hipField.clear();

        datePicker.setValue(LocalDate.now());

        clearNeckError();
        clearWaistError();
        clearHipError();
        clearDateError();

        clearActionError();
    }

    public void setEditMode(BodyMeasurementResponse measurement, Gender gender) {
        configureGender(gender);

        titleLabel.setText("Edit body measurement");
        saveButton.setText("Save changes");

        neckField.setText(NumberUtils.formatInputDecimal(measurement.neck()));
        waistField.setText(NumberUtils.formatInputDecimal(measurement.waist()));

        if (measurement.hip() != null) {
            hipField.setText(NumberUtils.formatInputDecimal(measurement.hip()));
        } else {
            hipField.clear();
        }

        datePicker.setValue(
                measurement.loggedAt()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
        );

        clearNeckError();
        clearWaistError();
        clearHipError();
        clearDateError();

        clearActionError();
    }

    public void setOnCancelAction(Runnable onCancelAction) {
        this.onCancelAction = onCancelAction;
    }

    public void setOnSaveAction(Consumer<BodyMeasurementRequest> onSaveAction) {
        this.onSaveAction = onSaveAction;
    }

    public void setOnNarrowLayoutChanged(Consumer<Boolean> onNarrowLayoutChanged) {
        this.onNarrowLayoutChanged = onNarrowLayoutChanged;
    }

    private void configureGender(Gender gender) {
        this.gender = gender;

        boolean showHip = gender == Gender.FEMALE;

        setVisible(hipGroup, showHip);

        if (!showHip) {
            hipField.clear();
            clearHipError();
        }
    }

    // ── Responsive ─────────────────────────────────────────────────
    @Override
    public void updateWidthLayout(boolean narrow) {
        rootLayout.pseudoClassStateChanged(NARROW, narrow);

        if (onNarrowLayoutChanged != null) {
            onNarrowLayoutChanged.accept(narrow);
        }
    }

    // ── Button Actions ─────────────────────────────────────────────
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

        AppDatePickerConfigurer.commitEditorValue(datePicker);

        clearActionError();

        if (!isFormValid()) {
            return;
        }

        double neck = NumberUtils.parseDecimal(neckField.getText().trim());
        double waist = NumberUtils.parseDecimal(waistField.getText().trim());

        Double hip = null;
        if (gender == Gender.FEMALE) {
            hip = NumberUtils.parseDecimal(hipField.getText().trim());
        }

        Instant loggedAt = datePicker.getValue()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        BodyMeasurementRequest request = new BodyMeasurementRequest(
                neck,
                waist,
                hip,
                loggedAt
        );

        if (onSaveAction != null) {
            onSaveAction.accept(request);
        }
    }

    private boolean isFormValid() {
        String neck = neckField.getText().trim();
        String waist = waistField.getText().trim();
        String hip = hipField.getText().trim();
        LocalDate date = datePicker.getValue();

        boolean valid = true;

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

        if (gender == Gender.FEMALE && !FitnessInputValidator.isHipValid(hip)) {
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

        if (date == null) {
            showDateMessage(AppConstants.Messages.INVALID_DATE_FORMAT_MESSAGE);
            shake(datePicker);
            valid = false;

        } else if (date.isAfter(LocalDate.now())) {
            showDateMessage(AppConstants.Messages.INVALID_MEASUREMENT_DATE_MESSAGE);
            shake(datePicker);
            valid = false;
        }

        return valid;
    }

    // ── Save State ─────────────────────────────────────────────────
    public void setSaving(boolean saving) {
        if (saving) {
            setLoading(saveButton, "Saving...");
        } else {
            resetLoading(saveButton);
        }
    }

    // ── Neck Helpers ───────────────────────────────────────────────
    private void showNeckMessage(String message) {
        setFieldMessage(neckMessage, message, true, neckField);
    }

    private void clearNeckError() {
        clearFieldMessage(neckMessage, neckField);
    }

    // ── Waist Helpers ──────────────────────────────────────────────
    private void showWaistMessage() {
        setFieldMessage(waistMessage, AppConstants.Messages.INVALID_WAIST_MESSAGE, true, waistField);
    }

    private void clearWaistError() {
        clearFieldMessage(waistMessage, waistField);
    }

    // ── Hip Helpers ────────────────────────────────────────────────
    private void showHipMessage() {
        setFieldMessage(hipMessage, AppConstants.Messages.INVALID_HIP_MESSAGE, true, hipField);
    }

    private void clearHipError() {
        clearFieldMessage(hipMessage, hipField);
    }

    // ── Date Helpers ───────────────────────────────────────────────
    private void showDateMessage(String message) {
        setFieldMessage(dateMessage, message, true, datePicker);
    }

    private void clearDateError() {
        clearFieldMessage(dateMessage, datePicker);
    }

    // ── Save Message Helpers ───────────────────────────────────────
    public void showActionError(String message) {
        setFormMessage(actionMessage, message, true);
    }

    private void clearActionError() {
        clearFormMessage(actionMessage);
    }
}