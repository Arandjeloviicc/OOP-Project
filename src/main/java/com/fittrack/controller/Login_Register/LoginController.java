package com.fittrack.controller.Login_Register;

import com.fittrack.model.User;
import com.fittrack.service.auth.AuthenticationResult;
import com.fittrack.service.auth.AuthenticationService;
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

public class LoginController extends FormController implements Initializable {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Override
    protected Logger getLogger() { return log; }

    @FXML private ImageView backgroundImage;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label toggleLabel;
    @FXML private Label toggleIcon;
    @FXML private Button loginButton;
    @FXML private StackPane rootPane;

    private final AuthenticationService authenticationService = new AuthenticationService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Load background image
        setBackgroundImage(backgroundImage);

        backgroundImage.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootPane.heightProperty());
        backgroundImage.setPreserveRatio(false);

        // Sync password fields on type
        initPasswordToggle(passwordField, passwordVisible);

        // Clear errors on type
        emailField.textProperty().addListener((obs, old, val) -> clearEmailError());
        passwordField.textProperty().addListener((obs, old, val) -> clearPasswordError());
        passwordVisible.textProperty().addListener((obs, old, val) -> clearPasswordError());
    }

    // ── Toggle password visibility ──────────────────────────────
    @FXML
    private void togglePassword() {
        togglePassword(passwordField, passwordVisible, toggleLabel, toggleIcon);
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
            showEmailError(AppConstants.Messages.INVALID_EMAIL_MESSAGE);
            shake(emailField);
            valid = false;
        }

        if (password.length() < AppConstants.Validation.MIN_PASSWORD_LENGTH) {
            showPasswordError(AppConstants.Messages.INVALID_PASSWORD_MESSAGE);
            shake(passwordShowing ? passwordVisible : passwordField);
            valid = false;
        }

        if (!valid) return;

        loginButton.setDisable(true);
        loginButton.setText("Logging in...");

        try {
            AuthenticationResult result = authenticationService.login(email, password);

            switch (result.status()) {
                case SUCCESS -> {
                    User user = result.user();

                    // Kasnije:
                    // UserSession.setCurrentUser(user);

                    log.info("Logged in successfully: {}", user.getEmail());

                    navigateTo(AppConstants.Views.DASHBOARD);
                }

                case USER_NOT_FOUND -> {
                    showEmailError("No account exists with this email.");
                    shake(emailField);

                    loginButton.setDisable(false);
                    loginButton.setText("Log in");
                }

                case WRONG_PASSWORD -> {
                    showPasswordError("Incorrect password.");
                    shake(passwordShowing ? passwordVisible : passwordField);

                    loginButton.setDisable(false);
                    loginButton.setText("Log in");
                }
            }
        } catch (IllegalArgumentException e) {
            log.error("Database error during login", e);
            resetLoginButton();
        }
    }

    // ── Register action ─────────────────────────────────────────
    @FXML
    private void handleRegister() {
        String enteredEmail = emailField.getText().trim();

        log.info("Navigate to register");

        RegisterController registerController = navigateTo(AppConstants.Views.REGISTER);

        registerController.prefillEmail(enteredEmail);
    }

    // ── Email Helpers ─────────────────────────────────────────────────
    private void showEmailError(String message) {
        emailError.setText(message);
        setFieldError(emailField, emailError, true);
    }

    private void clearEmailError() {
        setFieldError(emailField, emailError, false);
    }

    public void prefillEmail(String email) {
        if(email != null && !email.isBlank()) {
            emailField.setText(email);
        }
    }

    // ── Password Helpers ─────────────────────────────────────────────────
    private void showPasswordError(String message) {
        passwordError.setText(message);
        setFieldError(passwordField,   passwordError, true);
        setFieldError(passwordVisible, passwordError, true);
    }

    private void clearPasswordError() {
        setFieldError(passwordField,   passwordError, false);
        setFieldError(passwordVisible, passwordError, false);
    }

    // ── Login Button Helpers ─────────────────────────────────────────────────
    private void resetLoginButton() {
        loginButton.setDisable(false);
        loginButton.setText("Log in");
    }
}