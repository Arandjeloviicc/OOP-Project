package com.fittrack.controller.profile.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProfileNutritionGoalCardController {

    @FXML private Label goalTypeLabel;
    @FXML private Label weeklyPaceLabel;
    @FXML private Label activityLevelLabel;

    // Action
    private Runnable onEditAction;

    public void setData(String goalType, String weeklyPace, String activityLevel) {
        goalTypeLabel.setText(goalType);
        weeklyPaceLabel.setText(weeklyPace);
        activityLevelLabel.setText(activityLevel);
    }

    public void setOnEditAction(Runnable onEditAction) {
        this.onEditAction = onEditAction;
    }

    @FXML
    private void handleEdit() {
        if (onEditAction != null) {
            onEditAction.run();
        }
    }
}