package com.fittrack.controller.measurements.editor;

import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.FormController;

import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.dto.measurements.weight.WeightLogRequest;
import com.fittrack.dto.measurements.weight.WeightLogResponse;
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

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class WeightLogEditorController extends FormController implements Initializable, ResponsiveLayout {

    @FXML private StackPane rootLayout;
    @FXML private Label titleLabel;

    @FXML private TextField weightField;
    @FXML private Label weightMessage;

    @FXML private DatePicker datePicker;
    @FXML private Label dateMessage;

    @FXML private Label actionMessage;
    @FXML private Button saveButton;

    // Actions
    private Runnable onCancelAction;
    private Consumer<WeightLogRequest> onSaveAction;
    private Consumer<Boolean> onNarrowLayoutChanged;

    // Responsive
    private static final int NARROW_BREAKPOINT = 440;
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");

    // ── Initialization ─────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize controls and listeners
        initializeWeightLogControls();
        addListeners();

        // Keyboard Shortcuts
        SceneShortcuts.forNode(rootLayout)
                .onEscape(this::handleCancel)
                .onEnter(this::handleSave);
    }

    private void initializeWeightLogControls() {
        AppDatePickerConfigurer.configure(datePicker);
    }

    private void addListeners() {
        weightField.textProperty().addListener((observable, oldValue, newValue) -> {clearWeightError();});
        datePicker.getEditor().textProperty().addListener((obs, oldValue, newValue) -> clearDateError());
    }

    public void initializeResponsiveLayout(Region widthSource) {
        initializeResponsiveWidthLayout(widthSource, NARROW_BREAKPOINT);
    }

    // ── Configuration ─────────────────────────────────────────────────
    public void setCreateMode() {
        titleLabel.setText("Add weight");
        saveButton.setText("Add");

        weightField.clear();
        datePicker.setValue(LocalDate.now());

        clearWeightError();
        clearDateError();

        clearActionError();
    }

    public void setEditMode(WeightLogResponse weightLog) {
        titleLabel.setText("Edit weight");
        saveButton.setText("Save changes");

        weightField.setText(
                NumberUtils.formatInputDecimal(weightLog.weight())
        );

        datePicker.setValue(
                weightLog.loggedAt()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
        );

        clearWeightError();
        clearDateError();

        clearActionError();
    }

    public void setOnCancelAction(Runnable onCancelAction) {
        this.onCancelAction = onCancelAction;
    }

    public void setOnSaveAction(Consumer<WeightLogRequest> onSaveAction) {
        this.onSaveAction = onSaveAction;
    }

    public void setOnNarrowLayoutChanged(Consumer<Boolean> onNarrowLayoutChanged) {
        this.onNarrowLayoutChanged = onNarrowLayoutChanged;
    }

    // ── Responsive ─────────────────────────────────────────────────
    @Override
    public void updateWidthLayout(boolean narrow) {
        rootLayout.pseudoClassStateChanged(NARROW, narrow);

        if (onNarrowLayoutChanged != null) {
            onNarrowLayoutChanged.accept(narrow);
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

        AppDatePickerConfigurer.commitEditorValue(datePicker);

        clearActionError();

        if (!isFormValid()) {
            return;
        }

        double weight = NumberUtils.parseDecimal(weightField.getText().trim());

        Instant loggedAt = datePicker.getValue()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        WeightLogRequest request = new WeightLogRequest(
                weight,
                loggedAt
        );

        if (onSaveAction != null) {
            onSaveAction.accept(request);
        }
    }

    private boolean isFormValid() {
        String weight = weightField.getText().trim();
        LocalDate date = datePicker.getValue();

        boolean valid = true;

        if (!FitnessInputValidator.isWeightValid(weight)) {
            showWeightMessage();
            shake(weightField);
            valid = false;
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

    // ── Save State ─────────────────────────────────────────────
    public void setSaving(boolean saving) {
        if (saving) {
            setLoading(saveButton, "Saving...");
        } else {
            resetLoading(saveButton);
        }
    }

    // ── Weight Helpers ─────────────────────────────────────────────
    private void showWeightMessage() {
        setFieldMessage(weightMessage, AppConstants.Messages.INVALID_WEIGHT_MESSAGE, true, weightField);
    }

    private void clearWeightError() {
        clearFieldMessage(weightMessage, weightField);
    }

    // ── Date Helpers ─────────────────────────────────────────────
    private void showDateMessage(String message) {
        setFieldMessage(dateMessage, message, true, datePicker);
    }

    private void clearDateError() {
        clearFieldMessage(dateMessage, datePicker);
    }

    // ── Save message Helpers ─────────────────────────────────────────────────
    public void showActionError(String message) {
        setFormMessage(actionMessage, message, true);
    }

    private void clearActionError() {
        clearFormMessage(actionMessage);
    }
}