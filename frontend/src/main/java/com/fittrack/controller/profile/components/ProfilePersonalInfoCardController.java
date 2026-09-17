package com.fittrack.controller.profile.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProfilePersonalInfoCardController {

    @FXML private Label dateOfBirthLabel;
    @FXML private Label genderLabel;
    @FXML private Label heightLabel;

    // Action
    private Runnable onEditAction;

    public void setData(String dateOfBirth, String gender, String height) {
        dateOfBirthLabel.setText(dateOfBirth);
        genderLabel.setText(gender);
        heightLabel.setText(height);
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