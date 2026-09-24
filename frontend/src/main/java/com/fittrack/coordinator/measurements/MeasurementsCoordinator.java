package com.fittrack.coordinator.measurements;

import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.components.DeleteConfirmationController;
import com.fittrack.controller.measurements.editor.BodyMeasurementEditorController;
import com.fittrack.controller.measurements.editor.WeightLogEditorController;
import com.fittrack.dto.measurements.body.BodyMeasurementRequest;
import com.fittrack.dto.measurements.body.BodyMeasurementResponse;
import com.fittrack.dto.measurements.weight.WeightLogRequest;
import com.fittrack.dto.measurements.weight.WeightLogResponse;
import com.fittrack.model.profile.Gender;
import com.fittrack.ui.form.AppDatePickerConfigurer;
import com.fittrack.ui.loader.FxmlComponentLoader;
import com.fittrack.ui.loader.LoadedComponent;
import com.fittrack.ui.overlay.OverlayManager;
import com.fittrack.ui.popup.PopupShellController;
import com.fittrack.util.NumberUtils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class MeasurementsCoordinator {

    // Active Editors
    private WeightLogEditorController activeWeightLogEditor;
    private BodyMeasurementEditorController activeBodyMeasurementEditor;
    private DeleteConfirmationController activeDeleteConfirmation;

    private static final DateTimeFormatter DATE_FORMATTER = AppDatePickerConfigurer.displayFormatter();

    // ── Weight Log ─────────────────────────────────────────────────
    public void openCreateWeightLogEditor(Consumer<WeightLogRequest> onSave) {
        openWeightLogEditor(null, onSave);
    }

    public void openEditWeightLogEditor(WeightLogResponse weightLog, Consumer<WeightLogRequest> onSave) {
        openWeightLogEditor(weightLog, onSave);
    }

    private void openWeightLogEditor(WeightLogResponse weightLog, Consumer<WeightLogRequest> onSave) {
        LoadedComponent<WeightLogEditorController> editor = FxmlComponentLoader.load(AppConstants.Popups.WEIGHT_LOG_EDITOR);

        WeightLogEditorController controller = editor.controller();
        activeWeightLogEditor = controller;

        if (weightLog == null) {
            controller.setCreateMode();
        } else {
            controller.setEditMode(weightLog);
        }

        controller.setOnCancelAction(
                this::closeWeightLogEditor
        );

        controller.setOnSaveAction(request -> {
            if (onSave != null) {
                controller.setSaving(true);
                onSave.accept(request);
            }
        });

        PopupShellController shell =
                OverlayManager.showInPopup(
                        editor.root(),
                        () -> activeWeightLogEditor = null
                );

        controller.setOnNarrowLayoutChanged(
                shell::setContentTopAlignmentRequested
        );

        controller.initializeResponsiveLayout(
                shell.getRoot()
        );
    }

    public void setWeightLogSaving(boolean saving) {
        if (activeWeightLogEditor != null) {
            activeWeightLogEditor.setSaving(saving);
        }
    }

    public void showWeightLogSaveError(String message) {
        if (activeWeightLogEditor != null) {
            activeWeightLogEditor.showActionError(message);
        }
    }

    public void closeWeightLogEditor() {
        activeWeightLogEditor = null;
        OverlayManager.close();
    }

    public void openWeightLogDeleteConfirmation(WeightLogResponse weightLog, Runnable onDelete) {
        openDeleteConfirmation(
                "Delete weight entry?",
                "Are you sure you want to delete "
                        + NumberUtils.formatDecimal(weightLog.weight())
                        + " kg?",
                onDelete
        );
    }

    // ── Body Measurement ─────────────────────────────────────────────────
    public void openCreateBodyMeasurementEditor(Gender gender, Consumer<BodyMeasurementRequest> onSave) {
        openBodyMeasurementEditor(null, gender, onSave);
    }

    public void openEditBodyMeasurementEditor(BodyMeasurementResponse bodyMeasurement, Gender gender, Consumer<BodyMeasurementRequest> onSave) {
        openBodyMeasurementEditor(bodyMeasurement, gender, onSave);
    }

    private void openBodyMeasurementEditor(BodyMeasurementResponse bodyMeasurement, Gender gender, Consumer<BodyMeasurementRequest> onSave) {
        LoadedComponent<BodyMeasurementEditorController> editor = FxmlComponentLoader.load(AppConstants.Popups.BODY_MEASUREMENT_EDITOR);

        BodyMeasurementEditorController controller = editor.controller();

        activeBodyMeasurementEditor = controller;

        if (bodyMeasurement == null) {
            controller.setCreateMode(gender);
        } else {
            controller.setEditMode(bodyMeasurement, gender);
        }

        controller.setOnCancelAction(
                this::closeBodyMeasurementEditor
        );

        controller.setOnSaveAction(request -> {
            if (onSave != null) {
                controller.setSaving(true);
                onSave.accept(request);
            }
        });

        PopupShellController shell =
                OverlayManager.showInPopup(
                        editor.root(),
                        () -> activeBodyMeasurementEditor = null
                );

        controller.setOnNarrowLayoutChanged(
                shell::setContentTopAlignmentRequested
        );

        controller.initializeResponsiveLayout(
                shell.getRoot()
        );
    }

    public void setBodyMeasurementSaving(boolean saving) {
        if (activeBodyMeasurementEditor != null) {
            activeBodyMeasurementEditor.setSaving(saving);
        }
    }

    public void showBodyMeasurementSaveError(String message) {
        if (activeBodyMeasurementEditor != null) {
            activeBodyMeasurementEditor.showActionError(message);
        }
    }

    public void closeBodyMeasurementEditor() {
        activeBodyMeasurementEditor = null;
        OverlayManager.close();
    }

    public void openBodyMeasurementDeleteConfirmation(BodyMeasurementResponse bodyMeasurement, Runnable onDelete) {
        String date = bodyMeasurement.loggedAt()
                .atZone(ZoneId.systemDefault())
                .format(DATE_FORMATTER);

        openDeleteConfirmation(
                "Delete body measurement?",
                "Are you sure you want to delete the body measurement from "
                        + date + "?",
                onDelete
        );
    }

    // ── Delete Confirmation ─────────────────────────────────────────────────
    private void openDeleteConfirmation(String title, String message, Runnable onDelete) {
        LoadedComponent<DeleteConfirmationController> confirmation = FxmlComponentLoader.load(AppConstants.Popups.DELETE_CONFIRMATION);

        DeleteConfirmationController controller = confirmation.controller();

        activeDeleteConfirmation = controller;

        controller.setData(
                title,
                message,
                "Delete"
        );

        controller.setOnCancelAction(
                this::closeDeleteConfirmation
        );

        controller.setOnConfirmAction(() -> {
            if (onDelete != null) {
                controller.setDeleting(true);
                onDelete.run();
            }
        });

        OverlayManager.showModal(
                confirmation.root()
        );
    }

    public void setDeleteConfirmationDeleting(boolean deleting) {
        if (activeDeleteConfirmation != null) {
            activeDeleteConfirmation.setDeleting(deleting);
        }
    }

    public void showDeleteConfirmationError(String message) {
        if (activeDeleteConfirmation != null) {
            activeDeleteConfirmation.showDeleteError(message);
        }
    }

    public void closeDeleteConfirmation() {
        activeDeleteConfirmation = null;
        OverlayManager.closeModal();
    }
}