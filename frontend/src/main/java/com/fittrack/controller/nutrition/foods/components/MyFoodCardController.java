package com.fittrack.controller.nutrition.foods.components;

import com.fittrack.controller.common.BaseController;
import com.fittrack.dto.nutrition.food.FoodResponse;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class MyFoodCardController extends BaseController {

    @FXML private Label nameLabel;
    @FXML private Label detailsLabel;
    @FXML private Label caloriesLabel;
    @FXML private Button deleteButton;

    // Actions
    private Runnable onEditAction;
    private Runnable onDeleteAction;

    // ── Configuration ─────────────────────────────────────────────────────
    public void setData(FoodResponse food) {
        nameLabel.setText(food.name());

        String servingSize = Math.round(food.servingSizeGrams()) + " g";

        if (food.brand() == null || food.brand().isBlank()) {
            detailsLabel.setText(servingSize);
        } else {
            detailsLabel.setText(food.brand() + " · " + servingSize);
        }

        caloriesLabel.setText(Math.round(food.caloriesPerServing()) + " kcal");
    }

    public void setOnEditAction(Runnable onEditAction) {
        this.onEditAction = onEditAction;
    }

    public void setOnDeleteAction(Runnable onDeleteAction) {
        this.onDeleteAction = onDeleteAction;
    }


    // ── Actions ───────────────────────────────────────────────────────
    @FXML
    private void handleEdit(MouseEvent event) {
        if (isClickOnButton(event)) {
            return;
        }

        if (onEditAction != null) {
            onEditAction.run();
        }
    }

    @FXML
    private void handleDelete() {
        if (onDeleteAction != null) {
            onDeleteAction.run();
        }
    }

    // ── Helpers ─────────────────────────────────────────────────
    private boolean isClickOnButton(MouseEvent event) {
        Node node = (Node) event.getTarget();

        while (node != null) {
            if (node instanceof Button) {
                return true;
            }

            node = node.getParent();
        }

        return false;
    }
}
