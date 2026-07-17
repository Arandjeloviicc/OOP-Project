package com.fittrack.controller.Login_Register;

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
    @FXML private Label usernameMessage;
    @FXML private Label emailMessage;
    @FXML private Label passwordMessage;
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
        usernameField.textProperty().addListener((obs, old, val) -> restoreUsernameHelper());
        emailField.textProperty().addListener((obs, old, val) -> restoreEmailHelper());
        passwordField.textProperty().addListener((obs, old, val) -> restorePasswordHelper());
        passwordVisible.textProperty().addListener((obs, old, val) -> restorePasswordHelper());
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
            showUsernameMessage(AppConstants.Messages.INVALID_USERNAME_MESSAGE);
            shake(usernameField);
            valid = false;
        }

        if (!isValidEmail(email)) {
            showEmailMessage(AppConstants.Messages.INVALID_EMAIL_MESSAGE);
            shake(emailField);
            valid = false;
        }

        if (password.length() < AppConstants.Validation.MIN_PASSWORD_LENGTH) {
            showPasswordMessage();
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

                    // UserSession.setCurrentUser(user);

                    log.info("User registered successfully: {}", user.getEmail());

                    navigateTo(AppConstants.Views.PROFILE_SETUP);
                }

                case USERNAME_TAKEN -> {
                    showUsernameMessage("This username is already taken.");
                    shake(usernameField);
                    resetRegisterButton();
                }

                case EMAIL_TAKEN -> {
                    showEmailMessage("An account with this email already exists.");
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
        String enteredEmail = emailField.getText().trim();

        log.info("Navigate to login");

        LoginController loginController = navigateTo(AppConstants.Views.LOGIN);

        loginController.prefillEmail(enteredEmail);
    }

    // ── Helpers ─────────────────────────────────────────────────
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z][a-zA-Z0-9_]{%d,%d}$".formatted(
                    AppConstants.Validation.MIN_USERNAME_LENGTH - 1,
                    AppConstants.Validation.MAX_USERNAME_LENGTH - 1
            )
    );

    // ── Username Helpers ─────────────────────────────────────────────────
    private boolean isValidUsername(String username) {
        if (username == null || username.isBlank()) return false;
        return USERNAME_PATTERN.matcher(username).matches();
    }

    private void showUsernameMessage(String message) {
        setFieldMessage(usernameMessage, message, true, usernameField);
    }

    private void restoreUsernameHelper() {
        setFieldMessage(usernameMessage, AppConstants.Messages.HELPER_USERNAME_MESSAGE, false, usernameField);
    }

    // ── Email Helpers ─────────────────────────────────────────────────
    private void showEmailMessage(String message) {
        setFieldMessage(emailMessage, message, true, emailField);
    }

    private void restoreEmailHelper() {
        setFieldMessage(emailMessage, AppConstants.Messages.HELPER_USERNAME_MESSAGE, false, emailField);
    }

    public void prefillEmail(String email) {
        if(email != null && !email.isBlank()) {
            emailField.setText(email);
        }
    }

    // ── Password Helpers ─────────────────────────────────────────────────
    private void showPasswordMessage() {
        setFieldMessage(passwordMessage, AppConstants.Messages.INVALID_PASSWORD_MESSAGE, true, passwordField, passwordVisible);
    }

    private void restorePasswordHelper() {
        setFieldMessage(passwordMessage, AppConstants.Messages.HELPER_PASSWORD_MESSAGE, false, passwordField, passwordVisible);
    }

    // ── Register Button Helpers ─────────────────────────────────────────────────
    private void resetRegisterButton() {
        registerButton.setDisable(false);
        registerButton.setText("Register");
    }
}