package com.fittrack.controller.profile.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProfileAccountCardController {

    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;

    public void setData(String username, String email) {
        usernameLabel.setText(username);
        emailLabel.setText(email);
    }
}