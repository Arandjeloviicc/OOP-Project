package com.fittrack.controller.common.components;

import com.fittrack.controller.common.FormController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DeleteConfirmationController extends FormController {

    @FXML private Label titleLabel;
    @FXML private Label messageLabel;
    @FXML private Label actionMessage;
    @FXML private Button confirmButton;

    // Actions
    private Runnable onCancelAction;
    private Runnable onConfirmAction;

    // ── Configuration ────────────────────────────────────────
    public void setData(String title, String message, String confirmText) {
        titleLabel.setText(title);
        messageLabel.setText(message);
        confirmButton.setText(confirmText);
    }

    public void setOnCancelAction(Runnable onCancelAction) {
        this.onCancelAction = onCancelAction;
    }

    public void setOnConfirmAction(Runnable onConfirmAction) {
        this.onConfirmAction = onConfirmAction;
    }

    // ── Button Actions ───────────────────────────────────────
    @FXML
    private void handleCancel() {
        if (isLoading(confirmButton)) {
            return;
        }

        if (onCancelAction != null) {
            onCancelAction.run();
        }
    }

    @FXML
    private void handleConfirm() {
        if (isLoading(confirmButton)) {
            return;
        }

        clearDeleteError();

        if (onConfirmAction != null) {
            onConfirmAction.run();
        }
    }

    // ── Action Helpers ───────────────────────────────────────
    public void setDeleting(boolean deleting) {
        if (deleting) {
            setLoading(confirmButton, "Deleting...");
        } else {
            resetLoading(confirmButton);
        }
    }

    public void showDeleteError(String message) {
        setFormMessage(actionMessage, message, true);
    }

    private void clearDeleteError() {
        clearFormMessage(actionMessage);
    }
}