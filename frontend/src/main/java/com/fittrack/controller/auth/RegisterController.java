package com.fittrack.controller.auth;

import com.fittrack.api.auth.RegistrationApi;
import com.fittrack.model.user.User;
import com.fittrack.session.UserSession;
import com.fittrack.config.AppConstants;
import com.fittrack.async.AsyncTaskRunner;
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

public class RegisterController extends AuthFormController implements Initializable {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(RegisterController.class);

    @Override
    protected Logger getLogger() { return log; }

    @FXML private StackPane rootLayout;
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

    // API
    private final RegistrationApi authApi = new RegistrationApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Initialize all controls and images
        initializeAuthControls(rootLayout, backgroundImage, passwordField, passwordVisible);

        // Responsive initialize
        initializeAuthResponsiveLayout(rootLayout);

        // Initialize input messages
        restoreUsernameHelper();
        restoreEmailHelper();
        restorePasswordHelper();

        // Listeners
        addListeners();
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
        registerButton.setText("Registering...");

        AsyncTaskRunner.run(
            () -> authApi.register(username, email, password),

            result -> {
                switch (result.status()) {
                    case SUCCESS -> {
                        User user = result.user();

                        UserSession.getInstance().start(result.user());

                        log.info("User registered successfully: {}", user.email());

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
            },

            exception -> {
                log.warn("Registration failed: {}", exception.getMessage());

                resetRegisterButton();
            }
        );
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

    // ── Initialize Helpers ─────────────────────────────────────────────────
    private void addListeners() {
        usernameField.textProperty().addListener((obs, old, val) -> restoreUsernameHelper());
        emailField.textProperty().addListener((obs, old, val) -> restoreEmailHelper());
        passwordField.textProperty().addListener((obs, old, val) -> restorePasswordHelper());
        passwordVisible.textProperty().addListener((obs, old, val) -> restorePasswordHelper());
    }

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
        setFieldMessage(emailMessage, AppConstants.Messages.HELPER_EMAIL_MESSAGE, false, emailField);
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
        registerButton.setText("Create account");
    }
}