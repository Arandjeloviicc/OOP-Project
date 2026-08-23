package com.fittrack.controller.nutrition.components;

import com.fittrack.ui.QuickAddFeedback;
import com.fittrack.util.NumberUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class SavedMealListItemController {

    @FXML private Label nameLabel;
    @FXML private Label caloriesLabel;

    @FXML private Label carbsLabel;
    @FXML private Label fatLabel;
    @FXML private Label proteinLabel;

    @FXML private Button addButton;

    // Transition on Successful Meal Log
    private QuickAddFeedback addFeedback;

    // Action
    private Runnable onEditAction;
    private Runnable onAddAction;

    // ── Set Actions ────────────────────────────────────────────
    public void setOnEditAction(Runnable onEditAction) {
        this.onEditAction = onEditAction;
    }

    public void setOnAddAction(Runnable onAddAction) {
        this.onAddAction = onAddAction;
    }

    // ── Set Data ──────────────────────────────────────────────────
    public void setData(String name, double calories, double carbs, double fat, double protein) {
        nameLabel.setText(name);

        caloriesLabel.setText(NumberUtils.formatWhole(calories) + " cal");

        carbsLabel.setText("Carbs " + NumberUtils.formatWhole(carbs) + " g");
        fatLabel.setText("Fat " + NumberUtils.formatWhole(fat) + " g");
        proteinLabel.setText("Protein " + NumberUtils.formatWhole(protein) + " g");
    }

    // ── Button Actions ─────────────────────────────────────────
    @FXML
    private void handleEdit() {
        if (onEditAction != null) {
            onEditAction.run();
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
