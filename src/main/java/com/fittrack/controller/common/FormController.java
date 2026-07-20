package com.fittrack.controller.common;

import javafx.animation.TranslateTransition;
import javafx.scene.control.*;
import javafx.util.Duration;

public abstract class FormController extends BaseController {

    // ── Field errors ────────────────────────────────────────────
    protected void setFieldError(Control field, Label errorLabel, boolean show) {
        errorLabel.setVisible(show);

        if (show) {
            if(!field.getStyleClass().contains("error")) {
                field.getStyleClass().add("error");
            }
        } else {
            field.getStyleClass().removeAll("error");
        }
    }

    // ── Field messages ────────────────────────────────────────────
    protected void setFieldMessage(Label messageLabel, String message, boolean error, Control... fields) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);

        messageLabel.getStyleClass().removeAll(
                "helper-label",
                "error-label"
        );

        messageLabel.getStyleClass().add(
                error ? "error-label" : "helper-label"
        );

        for (Control field : fields) {
            if (error) {
                if (!field.getStyleClass().contains("error")) {
                    field.getStyleClass().add("error");
                }
            } else {
                field.getStyleClass().removeAll("error");
            }
        }
    }

    // Shake text fields when there is an error
    protected void shake(Control node) {
        TranslateTransition t = new TranslateTransition(Duration.millis(55), node);
        t.setFromX(0);
        t.setByX(9);
        t.setCycleCount(4);
        t.setAutoReverse(true);
        t.setOnFinished(e -> node.setTranslateX(0));
        t.play();
    }
}