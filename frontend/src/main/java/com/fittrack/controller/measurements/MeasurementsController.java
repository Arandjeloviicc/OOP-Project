package com.fittrack.controller.measurements;

import com.fittrack.api.common.ApiException;
import com.fittrack.async.AsyncTaskRunner;
import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.NavigableController;
import com.fittrack.controller.common.Refreshable;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.controller.measurements.components.BodyMeasurementItemController;
import com.fittrack.controller.measurements.components.MeasurementChartController;
import com.fittrack.controller.measurements.components.WeightLogItemController;
import com.fittrack.coordinator.measurements.MeasurementsCoordinator;
import com.fittrack.dto.measurements.body.BodyMeasurementRequest;
import com.fittrack.dto.measurements.body.BodyMeasurementResponse;
import com.fittrack.dto.measurements.weight.WeightLogRequest;
import com.fittrack.dto.measurements.weight.WeightLogResponse;
import com.fittrack.model.measurements.MeasurementType;
import com.fittrack.model.profile.Gender;
import com.fittrack.model.profile.WeightGoal;
import com.fittrack.service.measurements.BodyMeasurementService;
import com.fittrack.service.measurements.WeightLogService;
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
import java.util.Comparator;
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
    private static final int NARROW_BREAKPOINT = 450;
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");

    // Cached data
    private List<WeightLogResponse> weightLogs;
    private List<BodyMeasurementResponse> bodyMeasurements;

    // Chart
    private MeasurementChartController measurementChartController;

    // Helpers for WeightLogs and BodyMeasurements
    private WeightGoal weightGoal;
    private Gender gender;

    // Loading
    private boolean weightHistoryLoading;
    private boolean bodyHistoryLoading;

    // Coordinator
    private final MeasurementsCoordinator coordinator = new MeasurementsCoordinator();

    // Service
    private final WeightLogService weightLogService = new WeightLogService();
    private final BodyMeasurementService bodyMeasurementService = new BodyMeasurementService();

    // ── Initialization ────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize Responsive
        initializeResponsiveWidthLayout(rootLayout, NARROW_BREAKPOINT);

        // Initialize Controls
        initializeMeasurementsControls();
        initializeChart();

        // Weight Logs are loaded first
        loadWeightHistory();
    }

    private void initializeMeasurementsControls() {
        // Tabs
        tabButtons = List.of(weightTab, bodyMeasurementsTab);

        weightTab.setUserData(MeasurementType.WEIGHT);
        bodyMeasurementsTab.setUserData(MeasurementType.BODY_MEASUREMENT);

        measurementTypeGroup.selectToggle(weightTab);

        updateTabsLayout();

        measurementTypeGroup.selectedToggleProperty().addListener(
                (obs, oldToggle, newToggle) -> {
                    if (newToggle == null && oldToggle != null) {
                        measurementTypeGroup.selectToggle(oldToggle);
                        return;
                    }

                    if (newToggle != null) {
                        handleMeasurementTypeChanged();
                    }
                }
        );
    }

    private void handleMeasurementTypeChanged() {
        MeasurementType selectedType = (MeasurementType) measurementTypeGroup.getSelectedToggle().getUserData();

        switch (selectedType) {
            case WEIGHT -> {
                addEntryButton.setText("+ Add weight");
                addEntryButton.setDisable(weightLogs == null);

                loadWeightHistory();
            }

            case BODY_MEASUREMENT -> {
                addEntryButton.setText("+ Add body measurement");
                addEntryButton.setDisable(bodyMeasurements == null);

                loadBodyMeasurementHistory();
            }
        }
    }

    private void updateTabsLayout() {
        for (ToggleButton tab : tabButtons) {
            HBox.setHgrow(tab, Priority.ALWAYS);
            tab.setMaxWidth(Double.MAX_VALUE);
        }
    }

    // ── Chart ───────────────────────────────────────────────
    private void initializeChart() {
        LoadedComponent<MeasurementChartController> chart = FxmlComponentLoader.load(AppConstants.Components.MEASUREMENT_CHART);

        measurementChartController = chart.controller();

        chartContainer.getChildren().setAll(
                chart.root()
        );

        chartContainer.setVisible(true);
        chartContainer.setManaged(true);
    }

    // ── Weight Logs ───────────────────────────────────────────────
    private void loadWeightHistory() {
        if (weightLogs != null) {
            showWeightHistory();
            return;
        }

        if (weightHistoryLoading) {
            return;
        }

        weightHistoryLoading = true;

        if (isSelected(MeasurementType.WEIGHT)) {
            addEntryButton.setDisable(true);
        }

        AsyncTaskRunner.run(
                () -> weightLogService.getWeightHistory(),

                data -> {
                    weightHistoryLoading = false;

                    weightLogs = new ArrayList<>(data.logs());
                    weightGoal = data.goalType();

                    if (isSelected(MeasurementType.WEIGHT)) {
                        addEntryButton.setDisable(false);
                        showWeightHistory();
                    }
                },

                exception -> {
                    weightHistoryLoading = false;

                    log.error(
                            "Failed to load weight history.",
                            exception
                    );
                }
        );
    }

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

            component.controller().setOnEditAction(
                    () -> openEditWeightLogEditor(weightLog)
            );

            component.controller().setOnDeleteAction(
                    () -> openDeleteWeightLogConfirmation(weightLog)
            );

            historyContainer.getChildren().add(component.root());
        }

        updateChart();
    }

    private void openCreateWeightLogEditor() {
        coordinator.openCreateWeightLogEditor(
                this::createWeightLog
        );
    }

    private void createWeightLog(WeightLogRequest request) {
        AsyncTaskRunner.run(
                () -> weightLogService.createWeightLog(request),

                createdWeightLog -> {
                    weightLogs.add(createdWeightLog);

                    sortWeightLogs();
                    showWeightHistory();

                    coordinator.closeWeightLogEditor();
                },

                exception -> {
                    coordinator.setWeightLogSaving(false);

                    coordinator.showWeightLogSaveError(AppConstants.Messages.WEIGHT_LOG_CREATE_ERROR_MESSAGE);

                    log.error(
                            "Failed to create weight log.",
                            exception
                    );
                }
        );
    }

    private void openEditWeightLogEditor(WeightLogResponse weightLog) {
        coordinator.openEditWeightLogEditor(
                weightLog,
                request -> updateWeightLog(
                        weightLog.id(),
                        request
                )
        );
    }

    private void updateWeightLog(Integer weightLogId, WeightLogRequest request) {
        AsyncTaskRunner.run(
                () -> weightLogService.updateWeightLog(
                        weightLogId,
                        request
                ),

                updatedWeightLog -> {
                    replaceWeightLog(updatedWeightLog);

                    sortWeightLogs();
                    showWeightHistory();

                    coordinator.closeWeightLogEditor();
                },

                exception -> {
                    coordinator.setWeightLogSaving(false);

                    coordinator.showWeightLogSaveError(AppConstants.Messages.WEIGHT_LOG_UPDATE_ERROR_MESSAGE);

                    log.error(
                            "Failed to update weight log.",
                            exception
                    );
                }
        );
    }

    private void sortWeightLogs() {
        weightLogs.sort(
                Comparator
                        .comparing(
                                WeightLogResponse::loggedAt,
                                Comparator.reverseOrder()
                        )
                        .thenComparing(
                                WeightLogResponse::id,
                                Comparator.reverseOrder()
                        )
        );
    }

    private void replaceWeightLog(WeightLogResponse updatedWeightLog) {
        for (int i = 0; i < weightLogs.size(); i++) {
            if (weightLogs.get(i).id().equals(updatedWeightLog.id())) {
                weightLogs.set(i, updatedWeightLog);
                return;
            }
        }
    }

    private void openDeleteWeightLogConfirmation(WeightLogResponse weightLog) {
        coordinator.openWeightLogDeleteConfirmation(
                weightLog,
                () -> deleteWeightLog(weightLog.id())
        );
    }

    private void deleteWeightLog(Integer weightLogId) {
        AsyncTaskRunner.run(
                () -> {
                    weightLogService.deleteWeightLog(weightLogId);
                    return null;
                },

                ignored -> {
                    removeWeightLog(weightLogId);

                    showWeightHistory();

                    coordinator.closeDeleteConfirmation();
                },

                exception -> {
                    coordinator.setDeleteConfirmationDeleting(false);

                    String message = AppConstants.Messages.WEIGHT_LOG_DELETE_ERROR_MESSAGE;

                    if (exception instanceof ApiException apiException && Integer.valueOf(409).equals(apiException.getStatusCode())) {
                        coordinator.showDeleteConfirmationError(AppConstants.Messages.LAST_WEIGHT_LOG_DELETE_ERROR_MESSAGE);
                        return;
                    }

                    coordinator.showDeleteConfirmationError(message);

                    log.error(
                            "Failed to delete weight log.",
                            exception
                    );
                }
        );
    }

    private void removeWeightLog(Integer weightLogId) {
        weightLogs.removeIf(
                weightLog ->
                        weightLog.id().equals(weightLogId)
        );
    }

    // ── Body Measurements ───────────────────────────────────────────────
    private void loadBodyMeasurementHistory() {
        if (bodyMeasurements != null) {
            showBodyMeasurementHistory();
            return;
        }

        if (bodyHistoryLoading) {
            return;
        }

        bodyHistoryLoading = true;

        if (isSelected(MeasurementType.BODY_MEASUREMENT)) {
            addEntryButton.setDisable(true);
        }

        AsyncTaskRunner.run(
                bodyMeasurementService::getBodyMeasurementHistory,

                data -> {
                    bodyHistoryLoading = false;

                    bodyMeasurements = new ArrayList<>(data.measurements());

                    gender = data.gender();

                    if (isSelected(MeasurementType.BODY_MEASUREMENT)) {
                        addEntryButton.setDisable(false);
                        showBodyMeasurementHistory();
                    }
                },

                exception -> {
                    bodyHistoryLoading = false;

                    log.error(
                            "Failed to load body measurement history.",
                            exception
                    );
                }
        );
    }

    private void showBodyMeasurementHistory() {
        historyContainer.getChildren().clear();

        for (BodyMeasurementResponse bodyMeasurement : bodyMeasurements) {
            LoadedComponent<BodyMeasurementItemController> component = FxmlComponentLoader.load(AppConstants.Components.BODY_MEASUREMENT_ITEM);

            component.controller().setData(bodyMeasurement);

            component.controller().setOnEditAction(
                    () -> openEditBodyMeasurementEditor(bodyMeasurement)
            );

            component.controller().setOnDeleteAction(
                    () -> openDeleteBodyMeasurementConfirmation(bodyMeasurement)
            );

            historyContainer.getChildren().add(component.root());
        }

        updateChart();
    }

    private void openCreateBodyMeasurementEditor() {
        coordinator.openCreateBodyMeasurementEditor(
                gender,
                this::createBodyMeasurement
        );
    }

    private void createBodyMeasurement(BodyMeasurementRequest request) {
        AsyncTaskRunner.run(
                () -> bodyMeasurementService.createBodyMeasurement(request),

                createdBodyMeasurement -> {
                    bodyMeasurements.add(
                            createdBodyMeasurement
                    );

                    sortBodyMeasurements();
                    showBodyMeasurementHistory();

                    coordinator.closeBodyMeasurementEditor();
                },

                exception -> {
                    coordinator.setBodyMeasurementSaving(false);

                    coordinator.showBodyMeasurementSaveError(AppConstants.Messages.BODY_MEASUREMENT_CREATE_ERROR_MESSAGE);

                    log.error(
                            "Failed to create body measurement.",
                            exception
                    );
                }
        );
    }

    private void openEditBodyMeasurementEditor(BodyMeasurementResponse bodyMeasurement) {
        coordinator.openEditBodyMeasurementEditor(
                bodyMeasurement,
                gender,
                request -> updateBodyMeasurement(
                        bodyMeasurement.id(),
                        request
                )
        );
    }

    private void updateBodyMeasurement(Integer bodyMeasurementId, BodyMeasurementRequest request) {
        AsyncTaskRunner.run(
                () -> bodyMeasurementService.updateBodyMeasurement(
                        bodyMeasurementId,
                        request
                ),

                updatedBodyMeasurement -> {
                    replaceBodyMeasurement(updatedBodyMeasurement);

                    sortBodyMeasurements();
                    showBodyMeasurementHistory();

                    coordinator.closeBodyMeasurementEditor();
                },

                exception -> {
                    coordinator.setBodyMeasurementSaving(false);

                    coordinator.showBodyMeasurementSaveError(AppConstants.Messages.BODY_MEASUREMENT_UPDATE_ERROR_MESSAGE);

                    log.error(
                            "Failed to update body measurement.",
                            exception
                    );
                }
        );
    }

    private void sortBodyMeasurements() {
        bodyMeasurements.sort(
                Comparator
                        .comparing(
                                BodyMeasurementResponse::loggedAt,
                                Comparator.reverseOrder()
                        )
                        .thenComparing(
                                BodyMeasurementResponse::id,
                                Comparator.reverseOrder()
                        )
        );
    }

    private void replaceBodyMeasurement(BodyMeasurementResponse updatedBodyMeasurement) {
        for (int i = 0; i < bodyMeasurements.size(); i++) {
            if (bodyMeasurements.get(i).id().equals(updatedBodyMeasurement.id())) {
                bodyMeasurements.set(i, updatedBodyMeasurement);
                return;
            }
        }
    }

    private void openDeleteBodyMeasurementConfirmation(BodyMeasurementResponse bodyMeasurement) {
        coordinator.openBodyMeasurementDeleteConfirmation(
                bodyMeasurement,
                () -> deleteBodyMeasurement(bodyMeasurement.id())
        );
    }

    private void deleteBodyMeasurement(Integer bodyMeasurementId) {
        AsyncTaskRunner.run(
                () -> {
                    bodyMeasurementService.deleteBodyMeasurement(bodyMeasurementId);
                    return null;
                },

                ignored -> {
                    removeBodyMeasurement(bodyMeasurementId);

                    showBodyMeasurementHistory();

                    coordinator.closeDeleteConfirmation();
                },

                exception -> {
                    coordinator.setDeleteConfirmationDeleting(false);

                    coordinator.showDeleteConfirmationError(AppConstants.Messages.BODY_MEASUREMENT_DELETE_ERROR_MESSAGE);

                    log.error(
                            "Failed to delete body measurement.",
                            exception
                    );
                }
        );
    }

    private void removeBodyMeasurement(Integer bodyMeasurementId) {
        bodyMeasurements.removeIf(
                bodyMeasurement ->
                        bodyMeasurement.id().equals(bodyMeasurementId)
        );
    }

    // ── Responsive Helpers ─────────────────────────────────────────────────
    @Override
    public void updateWidthLayout(boolean narrow) {
        rootLayout.pseudoClassStateChanged(NARROW, narrow);
    }

    // ── Refresh Helpers ─────────────────────────────────────────────────
    @Override
    public void refresh() {
        weightLogs = null;
        bodyMeasurements = null;
        weightGoal = null;
        gender = null;

        MeasurementType selectedType = (MeasurementType) measurementTypeGroup.getSelectedToggle().getUserData();

        switch (selectedType) {
            case WEIGHT -> loadWeightHistory();
            case BODY_MEASUREMENT -> loadBodyMeasurementHistory();
        }
    }

    // ── Button Actions ────────────────────────────────────────────────
    @FXML
    private void handleAdd() {
        MeasurementType selectedType = (MeasurementType) measurementTypeGroup.getSelectedToggle().getUserData();

        switch (selectedType) {
            case WEIGHT -> openCreateWeightLogEditor();
            case BODY_MEASUREMENT -> openCreateBodyMeasurementEditor();
        }
    }

    // ── Helpers ────────────────────────────────────────────────
    private boolean isSelected(MeasurementType type) {
        if (measurementTypeGroup.getSelectedToggle() == null) {
            return false;
        }

        return measurementTypeGroup.getSelectedToggle().getUserData() == type;
    }

    private void updateChart() {
        if (measurementChartController == null) {
            return;
        }

        MeasurementType selectedType = (MeasurementType) measurementTypeGroup.getSelectedToggle().getUserData();

        switch (selectedType) {
            case WEIGHT -> {
                if (weightLogs != null) {
                    measurementChartController.setWeightData(
                            weightLogs,
                            weightGoal
                    );
                }
            }

            case BODY_MEASUREMENT -> {
                if (bodyMeasurements != null) {
                    measurementChartController.setBodyData(
                            bodyMeasurements,
                            gender
                    );
                }
            }
        }
    }
}