package com.fittrack.controller.nutrition.components;

import com.fittrack.util.NumberUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MealItemCardController {

    @FXML private Label titleLabel;
    @FXML private Label quantityLabel;
    @FXML private Label caloriesLabel;

    // Action
    private Runnable onOpenAction;

    // ── Set Action ─────────────────────────────────────────────────
    public void setOnOpenAction(Runnable onOpenAction) {
        this.onOpenAction = onOpenAction;
    }

    // ── Set Data ─────────────────────────────────────────────────
    public void setData(String title, double quantityGrams, double calories) {
        titleLabel.setText(title);

        quantityLabel.setText(NumberUtils.formatWhole(quantityGrams) + " g");

        caloriesLabel.setText(NumberUtils.formatWhole(calories) + " cal");
    }

    // ── Button Action ─────────────────────────────────────────────────
    @FXML
    private void handleOpen() {
        if (onOpenAction != null) {
            onOpenAction.run();
        }
    }
}
