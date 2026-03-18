package com.fittrack.controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class LoginController implements Initializable {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @FXML private ImageView backgroundImage;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label toggleLabel;
    @FXML private Label toggleIcon;
    @FXML private Button loginButton;

    private boolean passwordShowing = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Load background image
        try {
            Image bg = new Image(
                    Objects.requireNonNull(getClass().getResource("/com/fittrack/images/login-bg3.png")).toExternalForm()
            );
            backgroundImage.setImage(bg);
            backgroundImage.setFitWidth(1280);
            backgroundImage.setFitHeight(800);
            backgroundImage.setPreserveRatio(false);
        } catch (Exception e) {
            // Background image not found
            log.info("Background image not found: {}", e.getMessage());
        }

        // Sync password fields on type
        passwordField.textProperty().addListener((obs, old, val) -> {
            if (!passwordShowing) passwordVisible.setText(val);
        });
        passwordVisible.textProperty().addListener((obs, old, val) -> {
            if (passwordShowing) passwordField.setText(val);
        });

        // Clear errors on type
        emailField.textProperty().addListener((obs, old, val) -> clearEmailError());
        passwordField.textProperty().addListener((obs, old, val) -> clearPasswordError());
        passwordVisible.textProperty().addListener((obs, old, val) -> clearPasswordError());
    }

    // ── Toggle password visibility ──────────────────────────────
    @FXML
    private void togglePassword() {
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

    // ── Login action ────────────────────────────────────────────
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordShowing
                ? passwordVisible.getText()
                : passwordField.getText();

        boolean valid = true;

        if (!isValidEmail(email)) {
            showEmailError();
            shake(emailField);
            valid = false;
        }

        if (password.length() < 8) {
            showPasswordError();
            shake(passwordShowing ? passwordVisible : passwordField);
            valid = false;
        }

        if (!valid) return;

        loginButton.setDisable(true);
        loginButton.setText("Logging in...");

        // Simulate async login — replace with real auth logic
        PauseTransition delay = new PauseTransition(Duration.millis(1000));
        delay.setOnFinished(e -> onLoginSuccess());
        delay.play();
    }

    // ── Register action ─────────────────────────────────────────
    @FXML
    private void handleRegister() {
        // TODO: load register-view.fxml into the current scene
        log.info("Navigate to register");
    }

    // ── Success callback ────────────────────────────────────────
    private void onLoginSuccess() {
        // TODO: load dashboard-view.fxml
        log.info("Login successful!");
        loginButton.setDisable(false);
        loginButton.setText("Log in");
    }

    // ── Helpers ─────────────────────────────────────────────────
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[^\\s@]{1,64}@[^\\s@]{1,255}\\.[^\\s@.]{2,10}$"
    );

    private boolean isValidEmail(String email) {
        if (email == null || email.length() > 254) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private void setFieldError(Control field, Label errorLabel, boolean show) {
        errorLabel.setVisible(show);
        errorLabel.setManaged(show);
        if (show)
            field.getStyleClass().add("error");
        else
            field.getStyleClass().remove("error");
    }

    private void showEmailError() {
        setFieldError(emailField, emailError, true);
    }

    private void clearEmailError() {
        setFieldError(emailField, emailError, false);
    }

    private void showPasswordError() {
        setFieldError(passwordField,   passwordError, true);
        setFieldError(passwordVisible, passwordError, true);
    }

    private void clearPasswordError() {
        setFieldError(passwordField,   passwordError, false);
        setFieldError(passwordVisible, passwordError, false);
    }

    // Shake text fields when there is an error
    private void shake(Control node) {
        TranslateTransition t = new TranslateTransition(Duration.millis(55), node);
        t.setFromX(0);
        t.setByX(9);
        t.setCycleCount(4);
        t.setAutoReverse(true);
        t.setOnFinished(e -> node.setTranslateX(0));
        t.play();
    }
}