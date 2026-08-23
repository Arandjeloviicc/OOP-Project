package com.fittrack.controller.nutrition.components;

import com.fittrack.ui.QuickAddFeedback;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class FoodListItemController {

    @FXML private Label titleLabel;
    @FXML private Label detailsLabel;
    @FXML private Button addButton;

    // Transition on Successful Food Log
    private QuickAddFeedback addFeedback;

    // Actions
    private Runnable onOpenAction;
    private Runnable onAddAction;

    // ── Configure ─────────────────────────────────────────────
    public void setOnOpenAction(Runnable onOpenAction) {
        this.onOpenAction = onOpenAction;
    }

    public void setOnAddAction(Runnable onAddAction) {
        this.onAddAction = onAddAction;
    }

    public void setData(String title, double calories, double servingSizeGrams) {
        titleLabel.setText(title);

        detailsLabel.setText(Math.round(calories) + " cal, " + Math.round(servingSizeGrams) + " g");
    }

    // ── Button Actions ──────────────────────────────────────────
    @FXML
    private void handleOpen() {
        if (onOpenAction != null) {
            onOpenAction.run();
        }
    }

    @FXML
    private void handleAdd() {
        getAddFeedback().start();

        if (onAddAction != null) {
            onAddAction.run();
        }
    }

    // ── Transition ───────────────────────────────────────────────
    private QuickAddFeedback getAddFeedback() {
        if (addFeedback == null) {
            addFeedback = new QuickAddFeedback(addButton);
        }

        return addFeedback;
    }

    public void showAddSuccess() {
        getAddFeedback().success();
    }

    public void resetAddFeedback() {
        getAddFeedback().reset();
    }

}