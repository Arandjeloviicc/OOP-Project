package com.fittrack.controller.main;

import com.fittrack.controller.common.BaseController;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.session.UserSession;
import com.fittrack.util.AppConstants;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainLayoutController extends BaseController implements Initializable, ResponsiveLayout {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(MainLayoutController.class);

    @FXML private BorderPane rootLayout;
    @FXML private HBox headerBox;
    @FXML private FlowPane navigationContainer;
    @FXML private VBox sidebarUserContainer;
    @FXML private StackPane contentArea;
    @FXML private Label greetingLabel;
    @FXML private VBox sidebar;
    @FXML private HBox topBar;
    @FXML private HBox logoTopBar;
    @FXML private HBox logoSidebar;
    @FXML private Button logoutButtonSideBar;
    @FXML private Button logoutButtonHeader;

    // Sidebar
    @FXML private Label sidebarUsernameLabel;
    @FXML private ToggleGroup navigationGroup;
    @FXML private ToggleButton dashboardButton;
    @FXML private ToggleButton calculatorsButton;
    @FXML private ToggleButton mealsButton;
    @FXML private ToggleButton workoutsButton;
    @FXML private ToggleButton measurementsButton;
    @FXML private ToggleButton profileButton;
    @FXML private Button moreButton;

    // Constants
    private static final int NARROW_BREAKPOINT = 900;
    private static final int MAX_VISIBLE_NARROW = 4;

    // Adding PseudoClass for specific css styles
    private static final PseudoClass HORIZONTAL = PseudoClass.getPseudoClass("horizontal");

    // List of sidebar buttons
    private List<ToggleButton> navButtons;

    // Selected view
    private String currentView;

    // Is Narrow
    private Boolean narrowLayout;

    @Override
    protected Logger getLogger() { return log; }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Initialize all form controls
        initializeMainLayoutControls();

        // Initialize navigation
        initializeNavigationButtonState();
        preventNavigationDeselection();

        // Interface implemented method for responsive action
        initializeResponsiveLayout(rootLayout, NARROW_BREAKPOINT);
    }

    /* ── Sidebar Buttons ────────────────────────────────────────────── */
    @FXML
    private void handleDashboard() {
        showContent(dashboardButton, AppConstants.Views.DASHBOARD);
    }

    @FXML
    private void handleCalculators() {
        showContent(calculatorsButton, AppConstants.Views.CALCULATORS);
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
    private void handleProfile() {
        showContent(profileButton, AppConstants.Views.USER_PROFILE);
    }

    @FXML
    private void handleLogout() {
        UserSession.getInstance().end();
        navigateTo(AppConstants.Views.LOGIN);
    }

    /* ── Initialize Helpers ────────────────────────────────────────────── */
    private void initializeMainLayoutControls() {
        String username = UserSession.getInstance().requireCurrentUser().username();
        greetingLabel.setText("Hello, " + username);
        sidebarUsernameLabel.setText(username);

        greetingLabel.setText("Hello, username");
        sidebarUsernameLabel.setText("username");

        // List of all Sidebar buttons
        navButtons = List.of(dashboardButton, calculatorsButton, mealsButton, workoutsButton, measurementsButton, profileButton);

        // Default view - Dashboard
        navigationGroup.selectToggle(dashboardButton);

        currentView = AppConstants.Views.DASHBOARD;
        loadContent(currentView);
    }

    /* ── View Helpers ────────────────────────────────────────────── */
    // Switch scenes in the content area
    private void loadContent(String fxml) {
        try {
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

            contentArea.getChildren().setAll(content);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to load content: " + fxml,
                    exception
            );
        }
    }

    // Switch scenes and toggle button
    private void showContent(ToggleButton button, String view) {
        if(view.equals(currentView)) return;

        try {
            loadContent(view);
            navigationGroup.selectToggle(button);
            currentView = view;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            log.error("Failed to open view: {}", view, exception);
        }
    }

    // ── Responsive Helpers ─────────────────────────────────────────────────
    @Override
    public void updateLayout(boolean narrow) {
        if (Objects.equals(narrowLayout, narrow)) return;

        narrowLayout = narrow;

        if (narrow) {
            rootLayout.setTop(topBar);
            rootLayout.setLeft(null);
            rootLayout.setBottom(sidebar);
            navigationContainer.setOrientation(Orientation.HORIZONTAL);
        } else {
            rootLayout.setTop(null);
            rootLayout.setBottom(null);
            rootLayout.setLeft(sidebar);
            navigationContainer.setOrientation(Orientation.VERTICAL);
        }

        setVisible(topBar, narrow);

        setVisible(logoTopBar, narrow);

        setVisible(logoutButtonHeader, narrow);

        setVisible(logoSidebar, !narrow);

        setVisible(headerBox, !narrow);

        setVisible(sidebarUserContainer, !narrow);

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
            moreButton.setOnAction(e -> showContextMenu(overflow));
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

    // ── ContextMenu Helpers ─────────────────────────────────────────────────
    private void showContextMenu(List<ToggleButton> buttons) {
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("context-menu");

        List<CustomMenuItem> menuItems = new ArrayList<>();
        Map<ToggleButton, EventHandler<ActionEvent>> closeHandlers =
                new HashMap<>();

        for (ToggleButton button : buttons) {
            button.prefWidthProperty().unbind();
            button.getStyleClass().add("context-menu-navigation-button");

            CustomMenuItem item = new CustomMenuItem(button, false);

            menuItems.add(item);
            menu.getItems().add(item);

            EventHandler<ActionEvent> closeHandler = event -> menu.hide();

            button.addEventHandler(ActionEvent.ACTION, closeHandler);
            closeHandlers.put(button, closeHandler);
        }

        menu.setOnHidden(event ->
                Platform.runLater(() -> {
                    for (int i = 0; i < buttons.size(); i++) {
                        ToggleButton button = buttons.get(i);
                        CustomMenuItem item = menuItems.get(i);

                        button.removeEventHandler(ActionEvent.ACTION, closeHandlers.get(button));

                        item.setContent(null);

                        button.getStyleClass().remove("context-menu-navigation-button");
                    }
                })
        );

        menu.show(moreButton, Side.TOP, 0, 0);

        // Custom width and height depending on buttons
        Platform.runLater(() -> {
            Bounds moreButtonBounds = moreButton.localToScreen(moreButton.getBoundsInLocal());

            if (moreButtonBounds == null) {
                return;
            }

            double menuX = moreButtonBounds.getMaxX() - menu.getWidth();
            double menuY = moreButtonBounds.getMinY() - menu.getHeight();

            menu.setX(menuX);
            menu.setY(menuY);
        });
    }
}