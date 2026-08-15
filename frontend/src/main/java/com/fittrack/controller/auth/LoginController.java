package com.fittrack.controller.auth;

import javafx.scene.control.*;
import com.fittrack.api.auth.LoginApi;
import com.fittrack.api.profile.ProfileSetupApi;
import com.fittrack.model.user.User;
import com.fittrack.session.UserSession;
import com.fittrack.util.AppConstants;
import com.fittrack.util.AsyncTaskRunner;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

@SuppressWarnings("squid:S5411")
public class LoginController extends AuthFormController implements Initializable {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Override
    protected Logger getLogger() { return log; }

    @FXML private StackPane rootLayout;

    @FXML private ImageView backgroundImage;
    @FXML private TextField emailField;
    @FXML private Label emailMessage;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Label passwordMessage;
    @FXML private Label toggleLabel;
    @FXML private Label toggleIcon;
    @FXML private Button loginButton;

    // API
    private final LoginApi loginApi = new LoginApi();
    private final ProfileSetupApi profileSetupApi = new ProfileSetupApi();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Initialize all controls and images
        initializeAuthControls(rootLayout, backgroundImage, passwordField, passwordVisible);

        // Responsive initialize
        initializeAuthResponsiveLayout(rootLayout);

        // Initialize input messages
        restoreEmailHelper();
        restorePasswordHelper();

        // Listeners
        addListeners();

        // For testing
//        emailField.setText("petar.arandjelovic@gmail.com");
//        passwordField.setText("petar123");
//        handleLogin();
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
            showEmailMessage(AppConstants.Messages.INVALID_EMAIL_MESSAGE);
            shake(emailField);
            valid = false;
        }

        if (password.length() < AppConstants.Validation.MIN_PASSWORD_LENGTH) {
            showPasswordMessage(AppConstants.Messages.INVALID_PASSWORD_MESSAGE);
            shake(passwordShowing ? passwordVisible : passwordField);
            valid = false;
        }

        if (!valid) return;

        loginButton.setDisable(true);
        loginButton.setText("Logging in...");

        AsyncTaskRunner.run(
                () -> loginApi.login(email, password),

                result -> {
                    switch (result.status()) {
                        case SUCCESS -> {
                            User user = result.user();
                            UserSession session = UserSession.getInstance();
                            session.start(user);

                            log.info("Logged in successfully: {}", user.email());

                            AsyncTaskRunner.run(
                                () -> profileSetupApi.isProfileSetupComplete(user.id()),

                                profileSetupComplete -> {
                                    if (profileSetupComplete) {
                                        navigateTo(AppConstants.Views.MAIN_LAYOUT);
                                    } else {
                                        navigateTo(AppConstants.Views.PROFILE_SETUP);
                                    }
                                },

                                exception -> {
                                    session.end();

                                    log.error("Failed to check profile setup.", exception);
                                    showEmailMessage("Something went wrong while completing sign in. Please try again.");
                                    resetLoginButton();
                                }
                            );
                        }

                        case USER_NOT_FOUND -> {
                            showEmailMessage("No account exists with this email.");
                            shake(emailField);

                            loginButton.setDisable(false);
                            loginButton.setText("Log in");
                        }

                        case WRONG_PASSWORD -> {
                            showPasswordMessage("Incorrect password.");
                            shake(passwordShowing ? passwordVisible : passwordField);

                            loginButton.setDisable(false);
                            loginButton.setText("Log in");
                        }
                    }
                },

                exception -> {
                    log.error("Login request failed.", exception);
                    showEmailMessage("Something went wrong while signing in. Please try again.");
                    resetLoginButton();
                }
        );
    }

    // ── Register action ─────────────────────────────────────────
    @FXML
    private void handleRegister() {
        String enteredEmail = emailField.getText().trim();

        log.info("Navigate to register");

        RegisterController registerController = navigateTo(AppConstants.Views.REGISTER);

        registerController.prefillEmail(enteredEmail);
    }

    // ── Initialize Helpers ─────────────────────────────────────────────────
    private void addListeners() {
        emailField.textProperty().addListener((obs, old, val) -> restoreEmailHelper());
        passwordField.textProperty().addListener((obs, old, val) -> restorePasswordHelper());
        passwordVisible.textProperty().addListener((obs, old, val) -> restorePasswordHelper());
    }

    // ── Email Helpers ─────────────────────────────────────────────────
    private void showEmailMessage(String message) {
        setFieldMessage(emailMessage, message, true, emailField);
    }

    private void restoreEmailHelper() {
        setFieldMessage(emailMessage, "", false, emailField);
    }

    public void prefillEmail(String email) {
        if(email != null && !email.isBlank()) {
            emailField.setText(email);
        }
    }

    // ── Password Helpers ─────────────────────────────────────────────────
    private void showPasswordMessage(String message) {
        setFieldMessage(passwordMessage, message, true, passwordField, passwordVisible);
    }

    private void restorePasswordHelper() {
        setFieldMessage(passwordMessage, "", false, passwordField, passwordVisible);
    }

    // ── Login Button Helpers ─────────────────────────────────────────────────
    private void resetLoginButton() {
        loginButton.setDisable(false);
        loginButton.setText("Log in");
    }
}