package com.fittrack.controller.profile.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProfilePersonalInfoCardController {

    @FXML private Label dateOfBirthLabel;
    @FXML private Label genderLabel;
    @FXML private Label heightLabel;

    public void setData(String dateOfBirth, String gender, String height) {
        dateOfBirthLabel.setText(dateOfBirth);
        genderLabel.setText(gender);
        heightLabel.setText(height);
    }

    @FXML
    private void handleEdit() {
        // TODO document why this method is empty
    }
}