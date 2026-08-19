package com.fittrack.controller.nutrition.components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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

    // ── Button Actions ─────────────────────────────────────────────────
    @FXML
    public void handleLog() {
        if (onLogAction != null) {
            onLogAction.run();
        }
    }

    // ── Helpers ─────────────────────────────────────────────────
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

    public void setIcon(Image icon) {
        mealIcon.setImage(icon);
    }

    public void setOnLogAction(Runnable action) {
        this.onLogAction = action;
    }
}