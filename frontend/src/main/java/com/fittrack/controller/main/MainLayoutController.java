package com.fittrack.controller.main;

import com.fittrack.controller.common.NavigableController;
import com.fittrack.controller.common.Refreshable;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.ui.popup.PopupShellController;
import com.fittrack.model.view.ViewInstance;
import com.fittrack.service.auth.AuthService;
import com.fittrack.config.AppConstants;
import com.fittrack.ui.overlay.OverlayManager;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainLayoutController extends NavigableController implements Initializable, ResponsiveLayout {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(MainLayoutController.class);

    // Saves the data from the controllers and views
    private final Map<String, ViewInstance> contentCache = new HashMap<>();

    // Root
    @FXML private StackPane rootStack;
    @FXML private StackPane overlayContainer;
    @FXML private BorderPane rootLayout;

    // Top
    @FXML private HBox headerBox;
    @FXML private FlowPane navigationContainer;
    @FXML private StackPane contentArea;
    @FXML private Label greetingLabel;
    @FXML private VBox sidebar;
    @FXML private HBox topBar;
    @FXML private HBox logoTopBar;
    @FXML private HBox logoSidebar;

    // Sidebar
    @FXML private ToggleGroup navigationGroup;
    @FXML private ToggleButton dashboardButton;
    @FXML private ToggleButton calculatorsButton;
    @FXML private ToggleButton mealsButton;
    @FXML private ToggleButton workoutsButton;
    @FXML private ToggleButton measurementsButton;
    @FXML private ToggleButton profileButton;
    @FXML private ToggleButton moreButton;

    // Constants
    private static final int NARROW_BREAKPOINT = 900;
    private static final int MAX_VISIBLE_NARROW = 4;

    // Responsive
    private static final PseudoClass HORIZONTAL = PseudoClass.getPseudoClass("horizontal");
    private Boolean narrowLayout;

    // More Button
    private boolean morePopupOpen;

    // List of sidebar buttons
    private List<ToggleButton> navButtons;

    // Selected view
    private String currentView;

    // Service
    private final AuthService authService = new AuthService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Initialize all form controls
        initializeMainLayoutControls();

        // Initialize navigation
        initializeNavigationButtonState();
        preventNavigationDeselection();

        // Initialize Popup Field (Used for child views to show popups)
        OverlayManager.initialize(overlayContainer);

        // Interface implemented method for responsive action
        initializeResponsiveWidthLayout(rootStack, NARROW_BREAKPOINT);
    }

    /* ── Sidebar Buttons ────────────────────────────────────────────── */
    @FXML
    private void handleDashboard() {
        showContent(dashboardButton, AppConstants.Views.DASHBOARD);
    }

    @FXML
    private void handleMeals() {
        showContent(mealsButton, AppConstants.Views.MEALS);
    }

    @FXML
    private void handleWorkouts() {
        showContent(workoutsButton, AppConstants.Views.WORKOUTS);
    }

    @FXML
    private void handleMeasurements() {
        showContent(measurementsButton, AppConstants.Views.MEASUREMENTS);
    }

    @FXML
    private void handleCalculators() {
        showContent(calculatorsButton, AppConstants.Views.CALCULATORS);
    }

    @FXML
    private void handleProfile() {
        showContent(profileButton, AppConstants.Views.PROFILE);
    }

    /* ── Initialize Helpers ────────────────────────────────────────────── */
    private void initializeMainLayoutControls() {
        String username = authService.getCurrentUser().username();
        greetingLabel.setText("Hello, " + username);

        // List of all Sidebar buttons
        navButtons = List.of(dashboardButton, mealsButton, measurementsButton, calculatorsButton, profileButton);

        // Default view - Dashboard
        navigationGroup.selectToggle(dashboardButton);

        currentView = AppConstants.Views.DASHBOARD;
        loadContent(currentView);
    }

    /* ── View Helpers ────────────────────────────────────────────── */
    // Switch scenes in the content area
    private void loadContent(String fxml) {
        try {
            ViewInstance viewInstance = contentCache.get(fxml);
            boolean cached = viewInstance != null;

            if (!cached) {
                URL resource = getClass().getResource(
                        "/com/fittrack/view/" + fxml
                );

                if (resource == null) {
                    throw new IllegalArgumentException(
                            "FXML resource not found: " + fxml
                    );
                }

                FXMLLoader loader = new FXMLLoader(resource);
                Parent content = loader.load();
                Object controller = loader.getController();

                viewInstance = new ViewInstance(content, controller);
                contentCache.put(fxml, viewInstance);
            }

            contentArea.getChildren().setAll(viewInstance.root());

            if (cached && viewInstance.controller() instanceof Refreshable refreshable) {
                refreshable.refresh();
            }

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to load content: " + fxml,
                    exception
            );
        }
    }

    // Switch scenes and toggle button
    private void showContent(ToggleButton button, String view) {
        closeMorePopupIfOpen();

        if (view.equals(currentView)) {
            return;
        }

        try {
            loadContent(view);
            navigationGroup.selectToggle(button);
            currentView = view;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            log.error(
                    "Failed to open view: {}",
                    view,
                    exception
            );
        }
    }

    // ── Responsive Helpers ─────────────────────────────────────────────────
    @Override
    public void updateWidthLayout(boolean narrow) {
        if (Objects.equals(narrowLayout, narrow)) {
            return;
        }

        narrowLayout = narrow;

        if (!narrow) {
            closeMorePopupIfOpen();

            if (navigationGroup.getSelectedToggle() == moreButton) {
                navigationGroup.selectToggle(
                        getCurrentViewButton()
                );
            }
        }

        if (narrow) {
            rootLayout.setTop(topBar);
            rootLayout.setLeft(null);
            rootLayout.setBottom(sidebar);

            navigationContainer.setOrientation(Orientation.HORIZONTAL);
            navigationContainer.setPrefWrapLength(Double.MAX_VALUE);
        } else {
            rootLayout.setTop(null);
            rootLayout.setBottom(null);
            rootLayout.setLeft(sidebar);

            navigationContainer.setOrientation(Orientation.VERTICAL);
            navigationContainer.setPrefWrapLength(3000);
        }

        setVisible(topBar, narrow);
        setVisible(logoTopBar, narrow);
        setVisible(logoSidebar, !narrow);
        setVisible(headerBox, !narrow);

        sidebar.pseudoClassStateChanged(HORIZONTAL, narrow);

        updateNavigation(narrow);
    }

    // Toggle button - Block deselecting
    private void preventNavigationDeselection() {
        navigationGroup.selectedToggleProperty().addListener(
                (observable, oldToggle, newToggle) -> {
                    if (newToggle == null && oldToggle != null) {
                        navigationGroup.selectToggle(oldToggle);
                    }
                }
        );
    }

    // Toggle Button - Disable button after clicking
    private void initializeNavigationButtonState() {
        Toggle selectedToggle = navigationGroup.getSelectedToggle();

        if (selectedToggle instanceof ToggleButton selectedButton) {
            selectedButton.setDisable(true);
        }

        navigationGroup.selectedToggleProperty().addListener(
                (observable, oldToggle, newToggle) -> {

                    if (oldToggle instanceof ToggleButton oldButton) {
                        oldButton.setDisable(false);
                    }

                    if (newToggle instanceof ToggleButton newButton) {
                        newButton.setDisable(true);
                    }
                }
        );
    }

    // Sidebar - Responsive action
    private void updateNavigation(boolean narrow) {
        List<ToggleButton> visible;
        List<ToggleButton> overflow;

        boolean needsOverflow = narrow && navButtons.size() > MAX_VISIBLE_NARROW;

        if (needsOverflow) {
            visible = navButtons.subList(0, MAX_VISIBLE_NARROW - 1);
            overflow = navButtons.subList(MAX_VISIBLE_NARROW - 1, navButtons.size());
        } else {
            visible = navButtons;
            overflow = List.of();
        }

        navigationContainer.getChildren().setAll(visible);

        if (!overflow.isEmpty()) {
            navigationContainer.getChildren().add(moreButton);

            moreButton.setOnAction(
                    event -> showMorePopup(overflow)
            );
        }

        updateButtonWidths(narrow, visible, !overflow.isEmpty());
    }

    // ── Toggle Button Helpers ─────────────────────────────────────────────────
    private void updateButtonWidths(boolean narrow, List<ToggleButton> visible, boolean hasMore) {
        int count = visible.size() + (hasMore ? 1 : 0);

        for (ToggleButton button : visible) {
            button.prefWidthProperty().unbind();
            if (narrow) {
                button.prefWidthProperty().bind(sidebar.widthProperty().divide(count).subtract(1));
            } else {
                button.prefWidthProperty().bind(sidebar.widthProperty());
            }
        }

        moreButton.prefWidthProperty().unbind();
        if (hasMore) {
            moreButton.prefWidthProperty().bind(sidebar.widthProperty().divide(count).subtract(1));
        }
    }

    // ── More Popup ──────────────────────────────────────────────
    private void showMorePopup(List<ToggleButton> buttons) {
        if (!Boolean.TRUE.equals(narrowLayout) || morePopupOpen) {
            return;
        }

        VBox content = new VBox();
        content.getStyleClass().add("more-navigation-popup");

        for (ToggleButton sourceButton : buttons) {
            ToggleButton popupButton = createMoreNavigationButton(sourceButton);

            content.getChildren().add(popupButton);
        }

        StackPane.setMargin(
                overlayContainer,
                new Insets(
                        0,
                        0,
                        sidebar.getHeight(),
                        0
                )
        );

        setMorePopupOpen(true);

        PopupShellController shell =
                OverlayManager.showInPopup(
                        content,
                        () -> {
                            setMorePopupOpen(false);

                            StackPane.setMargin(
                                    overlayContainer,
                                    null
                            );
                        }
                );

        shell.setForceNarrow(true);
        shell.setContentTopAlignmentRequested(true);
    }

    private ToggleButton createMoreNavigationButton(ToggleButton sourceButton) {
        ToggleButton popupButton = new ToggleButton(sourceButton.getText());

        popupButton.setMaxWidth(Double.MAX_VALUE);

        popupButton.getStyleClass().addAll(
                "navigation-button",
                "more-navigation-popup-button"
        );

        popupButton.setSelected(sourceButton.isSelected());
        popupButton.setDisable(sourceButton.isDisable());
        popupButton.setVisible(sourceButton.isVisible());
        popupButton.setManaged(sourceButton.isManaged());

        if (sourceButton.getGraphic() instanceof ImageView sourceImage) {
            ImageView popupImage = new ImageView(sourceImage.getImage());

            popupImage.setFitWidth(sourceImage.getFitWidth());
            popupImage.setFitHeight(sourceImage.getFitHeight());
            popupImage.setPreserveRatio(sourceImage.isPreserveRatio());
            popupButton.setGraphic(popupImage);
        }

        popupButton.setOnAction(
                event -> sourceButton.fire()
        );

        return popupButton;
    }

    private void setMorePopupOpen(boolean open) {
        morePopupOpen = open;
    }

    private void closeMorePopupIfOpen() {
        if (morePopupOpen) {
            OverlayManager.close();
        }
    }

    // ── Helpers ──────────────────────────────────────────────
    private ToggleButton getCurrentViewButton() {
        if (AppConstants.Views.DASHBOARD.equals(currentView)) {
            return dashboardButton;
        }

        if (AppConstants.Views.CALCULATORS.equals(currentView)) {
            return calculatorsButton;
        }

        if (AppConstants.Views.MEALS.equals(currentView)) {
            return mealsButton;
        }

        if (AppConstants.Views.WORKOUTS.equals(currentView)) {
            return workoutsButton;
        }

        if (AppConstants.Views.MEASUREMENTS.equals(currentView)) {
            return measurementsButton;
        }

        if (AppConstants.Views.PROFILE.equals(currentView)) {
            return profileButton;
        }

        return dashboardButton;
    }
}