package com.fittrack.controller.profile.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProfileGoalsCardController {

    @FXML private Label goalTypeLabel;
    @FXML private Label weeklyPaceLabel;
    @FXML private Label activityLevelLabel;

    public void setData(String goalType, String weeklyPace, String activityLevel) {
        goalTypeLabel.setText(goalType);
        weeklyPaceLabel.setText(weeklyPace);
        activityLevelLabel.setText(activityLevel);
    }

    @FXML
    private void handleEdit() {
        // TODO document why this method is empty
    }
}