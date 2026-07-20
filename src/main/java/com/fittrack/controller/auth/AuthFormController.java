package com.fittrack.controller.auth;

import com.fittrack.controller.common.FormController;
import com.fittrack.util.AppConstants;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public abstract class AuthFormController extends FormController {

    // ── Validation ──────────────────────────────────────────────
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
    );

    protected boolean isValidEmail(String email) {
        if (email == null || email.length() > AppConstants.Validation.MAX_EMAIL_LENGTH) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    protected boolean passwordShowing = false;

    // ── Password toggle ─────────────────────────────────────────
    protected void initPasswordToggle(PasswordField passwordField, TextField passwordVisible) {
        passwordField.textProperty().addListener((obs, old, val) -> {
            if (!passwordShowing) passwordVisible.setText(val);
        });
        passwordVisible.textProperty().addListener((obs, old, val) -> {
            if (passwordShowing) passwordField.setText(val);
        });
    }

    protected void togglePassword(PasswordField passwordField,
                                  TextField passwordVisible,
                                  Label toggleLabel,
                                  Label toggleIcon) {
        passwordShowing = !passwordShowing;

        if (passwordShowing) {
            passwordVisible.setText(passwordField.getText());
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            toggleLabel.setText("Hide");
            toggleIcon.setText("⊘");
        } else {
            passwordField.setText(passwordVisible.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            toggleLabel.setText("Show");
            toggleIcon.setText("◉");
        }
    }

    // ── Background init ─────────────────────────────────────────
    protected void setBackgroundImage(ImageView backgroundImage) {
        try {
            Image bg = new Image(
                    Objects.requireNonNull(getClass().getResource("/com/fittrack/images/" + AppConstants.Images.LOGIN_REGISTER_BG)).toExternalForm()
            );
            backgroundImage.setImage(bg);
            backgroundImage.setPreserveRatio(false);
        } catch (Exception e) {
            // Background image not found
            getLogger().info("Background image not found: {}", e.getMessage());
        }
    }
}
