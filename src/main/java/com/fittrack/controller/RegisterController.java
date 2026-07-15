package com.fittrack.controller;

import com.fittrack.model.User;
import com.fittrack.service.auth.RegistrationResult;
import com.fittrack.service.auth.RegistrationService;
import com.fittrack.util.AppConstants;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
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
    @FXML private StackPane rootPane;

    private final RegistrationService registrationService = new RegistrationService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load background image
        setBackgroundImage(backgroundImage);

        backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());
        backgroundImage.setPreserveRatio(false);

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
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordShowing
                ? passwordVisible.getText()
                : passwordField.getText();

        boolean valid = true;

        if(!isValidUsername(username)) {
            showUsernameError("Username must contain 3–20 letters, numbers or underscores.");
            shake(usernameField);
            valid = false;
        }

        if (!isValidEmail(email)) {
            showEmailError("Enter a valid email address.");
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

        try {

            RegistrationResult result = registrationService.register(username, email, password);

            switch (result.status()) {
                case SUCCESS -> {
                    User user = result.user();

                    // Kasnije:
                    // UserSession.setCurrentUser(user);

                    log.info("User registered successfully: {}", user.getEmail());

                    navigateTo(AppConstants.Views.DASHBOARD);
                }

                case USERNAME_TAKEN -> {
                    showUsernameError("This username is already taken.");
                    shake(usernameField);
                    resetRegisterButton();
                }

                case EMAIL_TAKEN -> {
                    showEmailError("An account with this email already exists.");
                    shake(emailField);
                    resetRegisterButton();
                }
            }

        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());

            registerButton.setDisable(false);
            registerButton.setText("Create account");
        }
    }

    //  ── Login action ──────────────────────────────
    @FXML
    public void handleLogin() {
        // TODO: Logika za pamcenje podataka kada se vratim na login
        log.info("Navigate to login");
        navigateTo(AppConstants.Views.LOGIN);
    }

    // ── Helpers ─────────────────────────────────────────────────
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z][a-zA-Z0-9_]{2,19}$"
    );

    // ── Username Helpers ─────────────────────────────────────────────────
    private boolean isValidUsername(String username) {
        if (username == null || username.isBlank()) return false;
        return USERNAME_PATTERN.matcher(username).matches();
    }

    private void showUsernameError(String message) {
        usernameError.setText(message);
        setFieldError(usernameField, usernameError, true);
    }

    private void clearUsernameError() {
        setFieldError(usernameField, usernameError, false);
    }

    // ── Email Helpers ─────────────────────────────────────────────────
    private void showEmailError(String message) {
        emailError.setText(message);
        setFieldError(emailField, emailError, true);
    }

    private void clearEmailError() {
        setFieldError(emailField, emailError, false);
    }

    // ── Password Helpers ─────────────────────────────────────────────────
    private void showPasswordError() {
        setFieldError(passwordField,   passwordError, true);
        setFieldError(passwordVisible, passwordError, true);
    }

    private void clearPasswordError() {
        setFieldError(passwordField,   passwordError, false);
        setFieldError(passwordVisible, passwordError, false);
    }

    // ── Register Button Helpers ─────────────────────────────────────────────────
    private void resetRegisterButton() {
        registerButton.setDisable(false);
        registerButton.setText("Register");
    }
}