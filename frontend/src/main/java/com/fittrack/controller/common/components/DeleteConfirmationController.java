package com.fittrack.controller.common.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DeleteConfirmationController {

    @FXML private Label titleLabel;
    @FXML private Label messageLabel;
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
        if (onCancelAction != null) {
            onCancelAction.run();
        }
    }

    @FXML
    private void handleConfirm() {
        if (onConfirmAction != null) {
            onConfirmAction.run();
        }
    }
}