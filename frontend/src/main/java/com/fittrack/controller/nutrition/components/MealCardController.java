package com.fittrack.controller.nutrition.components;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.input.MouseEvent;

public class MealCardController {

    // Labels
    @FXML private Label titleLabel;
    @FXML private Label firstFoodLabel;
    @FXML private Label moreFoodsLabel;
    @FXML private Label caloriesLabel;

    // Other
    @FXML private Button menuButton;
    @FXML private Button logButton;
    @FXML private ImageView mealIcon;

    // Button Action
    private Runnable onLogAction;
    private Runnable onOpenAction;

    // ── Setters ─────────────────────────────────────────────────
    public void setIcon(Image icon) {
        mealIcon.setImage(icon);
    }

    public void setOnLogAction(Runnable action) {
        this.onLogAction = action;
    }

    public void setOnOpenAction(Runnable onOpenAction) {
        this.onOpenAction = onOpenAction;
    }

    // ── Button Actions ─────────────────────────────────────────────────
    @FXML
    private void handleLog() {
        if (onLogAction != null) {
            onLogAction.run();
        }
    }

    @FXML
    private void handleOpen(MouseEvent event) {
        if (isClickOnButton(event)) {
            return;
        }

        if (onOpenAction != null) {
            onOpenAction.run();
        }
    }

    // ── Set Data ─────────────────────────────────────────────────
    public void setData(String title, String firstFood, Integer otherFoodsCount, double calories) {
        titleLabel.setText(title);

        if (firstFood == null) {
            firstFoodLabel.setText("No foods logged");
            moreFoodsLabel.setText("");
            caloriesLabel.setText("0 cal");
        } else {
            firstFoodLabel.setText(firstFood);

            if (otherFoodsCount == null || otherFoodsCount <= 0) {
                moreFoodsLabel.setText("");
            } else {
                moreFoodsLabel.setText("and " + otherFoodsCount + " more");
            }

            caloriesLabel.setText(Math.round(calories) + " cal");
        }
    }

    // ── Button Helpers ─────────────────────────────────────────────────
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