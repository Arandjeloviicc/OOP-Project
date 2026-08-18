package com.fittrack.controller.nutrition.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


public class FoodListItemController {

    @FXML private Label titleLabel;
    @FXML private Label detailsLabel;
    @FXML private Button addButton;

    private Integer foodId;
    private Runnable onAddAction;

    // ── Button Actions ─────────────────────────────────────────────────
    @FXML
    public void handleAdd() {
        if (onAddAction != null) {
            onAddAction.run();
        }
    }

    // ── Helpers ─────────────────────────────────────────────────
    public void setData(Integer foodId, String title, double calories, double servingSizeGrams) {
        this.foodId = foodId;

        titleLabel.setText(title);

        detailsLabel.setText(Math.round(calories) + " cal, " + Math.round(servingSizeGrams) + " g");
    }

    public Integer getFoodId() {
        return foodId;
    }

    public void setOnAddAction(Runnable onAddAction) {
        this.onAddAction = onAddAction;
    }
}
