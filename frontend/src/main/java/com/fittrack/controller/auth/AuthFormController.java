package com.fittrack.controller.auth;

import com.fittrack.controller.common.FormController;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.util.AppConstants;
import com.fittrack.util.AppImages;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public abstract class AuthFormController extends FormController implements ResponsiveLayout {

    // Responsive breakpoint
    private static final int NARROW_BREAKPOINT = 450;

    // Is Narrow
    private Boolean narrowLayout;

    // PseudoClass for Narrow screen size
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");

    // Root StackPane
    private StackPane authRootPane;

    // ── Initialize Helpers ──────────────────────────────────────────────
    protected void initializeAuthControls(StackPane rootLayout, ImageView backgroundImage, PasswordField passwordField, TextField passwordVisible) {
        // Load background image
        //setBackgroundImage(backgroundImage);

        // Background size initialize
        backgroundImage.fitWidthProperty().bind(rootLayout.widthProperty());
        backgroundImage.fitHeightProperty().bind(rootLayout.heightProperty());
        backgroundImage.setPreserveRatio(false);

        // Sync password fields on type
        initPasswordToggle(passwordField, passwordVisible);
    }

    // ── Responsive Helpers ──────────────────────────────────────────────
    protected void initializeAuthResponsiveLayout(StackPane rootPane) {
        authRootPane = rootPane;
        initializeResponsiveLayout(authRootPane, NARROW_BREAKPOINT);
    }

    @Override
    public void updateLayout(boolean narrow) {
        if (Objects.equals(narrowLayout, narrow)) return;

        narrowLayout = narrow;

        authRootPane.pseudoClassStateChanged(NARROW, narrow);
    }

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
            backgroundImage.setImage(AppImages.LOGIN_REGISTER_BG);

            backgroundImage.setPreserveRatio(false);

        } catch (RuntimeException | ExceptionInInitializerError exception) {
            getLogger().info(
                    "Background image could not be loaded: {}",
                    exception.getMessage()
            );
        }
    }
}
