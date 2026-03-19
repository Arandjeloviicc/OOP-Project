package com.fittrack.controller;

import com.fittrack.util.AppConstants;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class RegisterController extends FormController implements Initializable {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(RegisterController.class);

    @Override
    protected Logger getLogger() { return log; }

    @FXML private ImageView backgroundImage;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Label usernameError;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label toggleLabel;
    @FXML private Label toggleIcon;
    @FXML private Button registerButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load background image
        setBackgroundImage(backgroundImage);

        // Sync password fields on type
        initPasswordToggle(passwordField, passwordVisible);

        // Clear errors on type
        usernameField.textProperty().addListener((obs, old, val) -> clearUsernameError());
        emailField.textProperty().addListener((obs, old, val) -> clearEmailError());
        passwordField.textProperty().addListener((obs, old, val) -> clearPasswordError());
        passwordVisible.textProperty().addListener((obs, old, val) -> clearPasswordError());
    }

    // ── Toggle password visibility ──────────────────────────────
    @FXML
    private void togglePassword() {
        togglePassword(passwordField, passwordVisible, toggleLabel, toggleIcon);
    }

    //  ── Register action ──────────────────────────────
    @FXML
    public void handleRegister() {
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

        if (password.length() < AppConstants.Validation.MIN_PASSWORD_LENGTH) {
            showPasswordError();
            shake(passwordShowing ? passwordVisible : passwordField);
            valid = false;
        }

        if (!valid) return;

        registerButton.setDisable(true);
        registerButton.setText("Registering in...");

        // Simulate async login — replace with real auth logic
        PauseTransition delay = new PauseTransition(Duration.millis(1000));
        delay.setOnFinished(e -> onRegisterSuccess());
        delay.play();
    }

    //  ── Login action ──────────────────────────────
    @FXML
    public void handleLogin() {
        // TODO: Logika za pamcenje podataka kada se vratim na login
        log.info("Navigate to register");
        navigateTo(AppConstants.Views.LOGIN);
    }

    // ── Success callback ────────────────────────────────────────
    private void onRegisterSuccess() {
        // TODO: load dashboard-view.fxml
        onActionSuccess(registerButton, "Register successful!", "Register");
    }

    // ── Helpers ─────────────────────────────────────────────────
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z][a-zA-Z0-9_]{2,19}$"
    );

    private boolean isValidUsername(String username) {
        if (username == null || username.isBlank()) return false;
        return USERNAME_PATTERN.matcher(username).matches();
    }

    private void showUsernameError() {
        setFieldError(usernameField, usernameError, true);
    }

    private void clearUsernameError() {
        setFieldError(usernameField, usernameError, false);
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
}
