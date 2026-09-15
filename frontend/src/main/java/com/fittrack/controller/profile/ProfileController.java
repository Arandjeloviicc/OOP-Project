package com.fittrack.controller.profile;

import com.fittrack.async.AsyncTaskRunner;
import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.NavigableController;
import com.fittrack.controller.common.Refreshable;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.controller.profile.components.*;
import com.fittrack.model.profile.ProfileData;
import com.fittrack.service.auth.AuthService;
import com.fittrack.service.profile.ProfileService;
import com.fittrack.ui.FxmlComponentLoader;
import com.fittrack.ui.LoadedComponent;
import com.fittrack.ui.OverlayManager;
import com.fittrack.ui.SceneManager;
import com.fittrack.util.NumberUtils;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ProfileController extends NavigableController implements Initializable, ResponsiveLayout, Refreshable {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    // Root
    @FXML private StackPane rootLayout;
    @FXML private ScrollPane profileScroll;
    @FXML private VBox contentContainer;

    // Header
    @FXML private HBox profileHeader;
    @FXML private Label initialsLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;

    // Component Containers
    @FXML private VBox weightCardContainer;
    @FXML private VBox goalsCardContainer;
    @FXML private VBox targetsCardContainer;
    @FXML private VBox personalInfoCardContainer;
    @FXML private VBox accountCardContainer;

    // Cards
    private ProfileWeightCardController weightCardController;
    private ProfileGoalsCardController goalsCardController;
    private ProfileTargetsCardController targetsCardController;
    private ProfilePersonalInfoCardController personalInfoCardController;
    private ProfileAccountCardController accountCardController;

    // Responsive
    private static final int NARROW_BREAKPOINT = 400;
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");

    // Date Format
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.yyyy");

    // Service
    private final AuthService authService = new AuthService();
    private final ProfileService profileService = new ProfileService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeComponents();

        scrollToTop();
        loadProfile();
    }

    // ── Initialize Helpers ─────────────────────────────────────────────────
    private void initializeComponents() {
        // Initialize cards
        initializeWeightCardController();
        initializeGoalsCardController();
        initializeTargetsCardController();
        initializePersonalInfoCardController();
        initializeAccountCardController();

        // Initialize responsive
        initializeResponsiveWidthLayout(rootLayout, NARROW_BREAKPOINT);
    }

    private void initializeWeightCardController() {
        LoadedComponent<ProfileWeightCardController> weightCard = FxmlComponentLoader.load(AppConstants.Components.PROFILE_WEIGHT_CARD);

        weightCardController = weightCard.controller();

        weightCardContainer.getChildren().setAll(weightCard.root());
    }

    private void initializeGoalsCardController() {
        LoadedComponent<ProfileGoalsCardController> goalsCard = FxmlComponentLoader.load(AppConstants.Components.PROFILE_GOALS_CARD);

        goalsCardController = goalsCard.controller();

        goalsCardContainer.getChildren().setAll(goalsCard.root());
    }

    private void initializeTargetsCardController() {
        LoadedComponent<ProfileTargetsCardController> targetsCard = FxmlComponentLoader.load(AppConstants.Components.PROFILE_TARGETS_CARD);

        targetsCardController = targetsCard.controller();

        targetsCardContainer.getChildren().setAll(targetsCard.root());
    }

    private void initializePersonalInfoCardController() {
        LoadedComponent<ProfilePersonalInfoCardController> infoCard = FxmlComponentLoader.load(AppConstants.Components.PROFILE_INFO_CARD);

        personalInfoCardController = infoCard.controller();

        personalInfoCardContainer.getChildren().setAll(infoCard.root());
    }

    private void initializeAccountCardController() {
        LoadedComponent<ProfileAccountCardController> accountCard = FxmlComponentLoader.load(AppConstants.Components.PROFILE_ACCOUNT_CARD);

        accountCardController = accountCard.controller();

        accountCardContainer.getChildren().setAll(accountCard.root());
    }

    private void loadProfile() {
        AsyncTaskRunner.run(
                profileService::getProfile,

                this::showProfile,

                exception -> log.error(
                        "Failed to load profile.",
                        exception
                )
        );
    }

    private void showProfile(ProfileData profile) {
        showHeader(profile);

        weightCardController.setData(
                profile.currentWeight(),
                profile.startWeight(),
                profile.goalWeight()
        );

        goalsCardController.setData(
                profile.goalType().toString(),
                formatWeeklyGoal(profile.weeklyGoal()),
                profile.activityLevel().toString()
        );

        targetsCardController.setData(
                profile.targetCalories(),
                profile.targetCarbs(),
                profile.targetFat(),
                profile.targetProtein()
        );

        personalInfoCardController.setData(
                formatDate(profile.dateOfBirth()),
                profile.gender().toString(),
                formatHeight(profile.height())
        );

        accountCardController.setData(
                profile.username(),
                profile.email()
        );
    }

    private void showHeader(ProfileData profile) {
        fullNameLabel.setText(
                profile.firstName() + " " + profile.lastName()
        );

        emailLabel.setText(profile.email());

        initialsLabel.setText(
                getInitials(
                        profile.firstName(),
                        profile.lastName()
                )
        );
    }

    // ── Format Helpers ─────────────────────────────────────────────────
    private String getInitials(String firstName, String lastName) {
        StringBuilder initials = new StringBuilder();

        if (firstName != null && !firstName.isBlank()) {
            initials.append(Character.toUpperCase(firstName.trim().charAt(0)));
        }

        if (lastName != null && !lastName.isBlank()) {
            initials.append(Character.toUpperCase(lastName.trim().charAt(0)));
        }

        return initials.isEmpty() ? "?" : initials.toString();
    }

    private String formatWeeklyGoal(Double weeklyGoal) {
        if (weeklyGoal == null) {
            return "Maintain";
        }

        return NumberUtils.formatInputDecimal(weeklyGoal) + " kg / week";
    }

    private String formatHeight(double height) {
        return NumberUtils.formatDecimal(height) + " cm";
    }

    private String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    // ── Responsive Helpers ─────────────────────────────────────────────────
    @Override
    public void updateWidthLayout(boolean narrow) {
        rootLayout.pseudoClassStateChanged(NARROW, narrow);
    }

    // ── Refresh Helpers ─────────────────────────────────────────────────
    @Override
    public void refresh() {
        loadProfile();
    }

    // ── Button Actions ─────────────────────────────────────────────────
    @FXML
    private void handleLogout() {
        OverlayManager.close();
        authService.logout();
        SceneManager.clearCache();

        navigateTo(AppConstants.Views.LOGIN);
    }

    // ── ScrollPane Actions ─────────────────────────────────────────────────
    private void scrollToTop() {
        Platform.runLater(() ->
                profileScroll.setVvalue(profileScroll.getVmin())
        );
    }
}