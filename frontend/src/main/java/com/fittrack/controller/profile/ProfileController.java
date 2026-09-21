package com.fittrack.controller.profile;

import com.fittrack.async.AsyncTaskRunner;
import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.NavigableController;
import com.fittrack.controller.common.Refreshable;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.controller.profile.components.*;
import com.fittrack.coordinator.profile.ProfileCoordinator;
import com.fittrack.dto.profile.editor.NutritionGoalUpdateRequest;
import com.fittrack.dto.profile.editor.PersonalInfoUpdateRequest;
import com.fittrack.model.profile.ProfileData;
import com.fittrack.service.auth.AuthService;
import com.fittrack.service.profile.ProfileService;
import com.fittrack.ui.loader.FxmlComponentLoader;
import com.fittrack.ui.loader.LoadedComponent;
import com.fittrack.ui.overlay.OverlayManager;
import com.fittrack.ui.scene.SceneManager;
import com.fittrack.util.NumberUtils;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
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
    @FXML private VBox nutritionGoalCardContainer;
    @FXML private VBox targetsCardContainer;
    @FXML private VBox personalInfoCardContainer;
    @FXML private VBox accountCardContainer;

    // Logout
    @FXML private Button logoutButton;

    // Cards
    private ProfileWeightCardController weightCardController;
    private ProfileNutritionGoalCardController nutritionGoalCardController;
    private ProfileTargetsCardController targetsCardController;
    private ProfilePersonalInfoCardController personalInfoCardController;
    private ProfileAccountCardController accountCardController;

    // Responsive
    private static final int NARROW_BREAKPOINT = 400;
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");

    // Date Format
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.yyyy");

    // Coordinator
    private final ProfileCoordinator coordinator = new ProfileCoordinator();
    private ProfileData profileData;

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
        initializeNutritionGoalCardController();
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

    private void initializeNutritionGoalCardController() {
        LoadedComponent<ProfileNutritionGoalCardController> nutritionGoalCard = FxmlComponentLoader.load(AppConstants.Components.PROFILE_NUTRITION_GOAL_CARD);

        nutritionGoalCardController = nutritionGoalCard.controller();

        nutritionGoalCardController.setOnEditAction(
                this::openNutritionGoalEditor
        );

        nutritionGoalCardContainer.getChildren().setAll(nutritionGoalCard.root());
    }

    private void initializeTargetsCardController() {
        LoadedComponent<ProfileTargetsCardController> targetsCard = FxmlComponentLoader.load(AppConstants.Components.PROFILE_TARGETS_CARD);

        targetsCardController = targetsCard.controller();

        targetsCardContainer.getChildren().setAll(targetsCard.root());
    }

    private void initializePersonalInfoCardController() {
        LoadedComponent<ProfilePersonalInfoCardController> infoCard = FxmlComponentLoader.load(AppConstants.Components.PROFILE_PERSONAL_INFO_CARD);

        personalInfoCardController = infoCard.controller();

        personalInfoCardController.setOnEditAction(
                this::openPersonalInfoEditor
        );

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
        this.profileData = profile;

        showHeader(profile);

        weightCardController.setData(
                profile.currentWeight(),
                profile.startWeight(),
                profile.goalWeight()
        );

        nutritionGoalCardController.setData(
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

    // ── Personal Info ─────────────────────────────────────────────────
    private void openPersonalInfoEditor() {
        if (profileData == null) {
            return;
        }

        coordinator.openPersonalInfoEditor(
                profileData,
                this::updatePersonalInfo
        );
    }

    private void updatePersonalInfo(PersonalInfoUpdateRequest request) {
        AsyncTaskRunner.run(
                () -> {
                    profileService.updatePersonalInfo(request);
                    return null;
                },

                ignored -> {
                    coordinator.closePersonalInfoEditor();
                    loadProfile();
                },

                exception -> {
                    log.error(
                            "Failed to update personal info.",
                            exception
                    );

                    coordinator.setPersonalInfoSaving(false);
                    coordinator.showPersonalInfoSaveError("Failed to save changes. Please try again.");
                }
        );
    }

    // ── Nutrition Goal ─────────────────────────────────────────────────
    private void openNutritionGoalEditor() {
        if (profileData == null) {
            return;
        }

        coordinator.openNutritionGoalEditor(
                profileData,
                this::updateNutritionGoal
        );
    }

    private void updateNutritionGoal(NutritionGoalUpdateRequest request) {
        AsyncTaskRunner.run(
                () -> {
                    profileService.updateNutritionalGoal(request);
                    return null;
                },

                ignored -> {
                    coordinator.closeNutritionGoalEditor();
                    loadProfile();
                },

                exception -> {
                    log.error(
                            "Failed to update nutritional goal.",
                            exception
                    );

                    coordinator.setNutritionGoalSaving(false);
                    coordinator.showNutritionGoalSaveError("Failed to save changes. Please try again.");
                }
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

        HBox.setHgrow(
                logoutButton,
                narrow ? Priority.ALWAYS : Priority.NEVER
        );
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