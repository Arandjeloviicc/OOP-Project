package com.fittrack.controller.measurements;

import com.fittrack.async.AsyncTaskRunner;
import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.NavigableController;
import com.fittrack.controller.common.Refreshable;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.controller.measurements.components.WeightLogItemController;
import com.fittrack.dto.measurements.weight.WeightLogResponse;
import com.fittrack.model.measurement.MeasurementType;
import com.fittrack.model.measurement.WeightHistoryData;
import com.fittrack.model.profile.WeightGoal;
import com.fittrack.service.measurements.WeightLogService;
import com.fittrack.service.profile.ProfileService;
import com.fittrack.ui.loader.FxmlComponentLoader;
import com.fittrack.ui.loader.LoadedComponent;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MeasurementsController extends NavigableController implements Initializable, Refreshable, ResponsiveLayout {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(MeasurementsController.class);

    // Root
    @FXML private StackPane rootLayout;
    @FXML private ScrollPane measurementsScroll;

    // Tabs
    @FXML private ToggleGroup measurementTypeGroup;
    @FXML private ToggleButton weightTab;
    @FXML private ToggleButton bodyMeasurementsTab;

    // Containers
    @FXML private VBox chartContainer;
    @FXML private VBox historyContainer;

    // Add Button
    @FXML private Button addEntryButton;

    // Tabs
    private List<ToggleButton> tabButtons;

    // Responsive
    private static final int NARROW_BREAKPOINT = 500;
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");

    // Cached data
    private List<WeightLogResponse> weightLogs = new ArrayList<>();
    private WeightGoal weightGoal;

    // Service
    private final WeightLogService weightLogService = new WeightLogService();
    private final ProfileService profileService = new ProfileService();

    // ── Initialization ────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize Responsive
        initializeResponsiveWidthLayout(rootLayout, NARROW_BREAKPOINT);

        // Initialize Controls
        initializeMeasurementsControls();

        loadWeightHistory();
    }

    private void initializeMeasurementsControls() {
        // Tabs
        tabButtons = List.of(weightTab, bodyMeasurementsTab);
        weightTab.setUserData(MeasurementType.WEIGHT);
        bodyMeasurementsTab.setUserData(MeasurementType.BODY_MEASUREMENT);
        measurementTypeGroup.selectToggle(weightTab);
        updateTabsLayout();

        measurementTypeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                    measurementTypeGroup.selectToggle(oldToggle);
                }

        });
    }

    private void updateTabsLayout() {
        for (ToggleButton tab : tabButtons) {
            HBox.setHgrow(tab, Priority.ALWAYS);
            tab.setMaxWidth(Double.MAX_VALUE);
        }
    }

    // ── Responsive Helpers ─────────────────────────────────────────────────
    @Override
    public void updateWidthLayout(boolean narrow) {
        rootLayout.pseudoClassStateChanged(NARROW, narrow);
    }

    // ── Refresh Helpers ─────────────────────────────────────────────────
    @Override
    public void refresh() {
        // TODO document why this method is empty
    }

    // ── Load Data ───────────────────────────────────────────────
    private void loadWeightHistory() {
        AsyncTaskRunner.run(
                () -> new WeightHistoryData(
                        weightLogService.getWeightHistory(),
                        profileService.getProfile().goalType()
                ),

                data -> {
                    weightLogs = new ArrayList<>(data.logs());
                    weightGoal = data.goalType();

                    showWeightHistory();
                },

                exception ->
                        log.error(
                                "Failed to load weight history.",
                                exception
                        )
        );
    }

    // ── History ─────────────────────────────────────────────────
    private void showWeightHistory() {
        historyContainer.getChildren().clear();

        for (int i = 0; i < weightLogs.size(); i++) {
            WeightLogResponse weightLog = weightLogs.get(i);

            Double previousWeight = null;

            if (i + 1 < weightLogs.size()) {
                previousWeight = weightLogs.get(i + 1).weight();
            }

            LoadedComponent<WeightLogItemController> component = FxmlComponentLoader.load(AppConstants.Components.WEIGHT_LOG_ITEM);

            component.controller().setData(
                    weightLog,
                    previousWeight,
                    weightGoal
            );

            historyContainer.getChildren().add(
                    component.root()
            );
        }
    }

    // ── Button Actions ────────────────────────────────────────────────
    @FXML
    private void handleAdd() {
        // TODO document why this method is empty
    }
}