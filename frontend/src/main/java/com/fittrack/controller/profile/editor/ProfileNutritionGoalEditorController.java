package com.fittrack.controller.profile.editor;

import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.FormController;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.dto.profile.editor.NutritionGoalUpdateRequest;
import com.fittrack.model.profile.ActivityLevel;
import com.fittrack.model.profile.ProfileData;
import com.fittrack.model.profile.WeightGoal;
import com.fittrack.ui.scene.SceneShortcuts;
import com.fittrack.util.NumberUtils;
import com.fittrack.validation.FitnessInputValidator;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
public class ProfileNutritionGoalEditorController extends FormController implements Initializable, ResponsiveLayout {

    // Root
    @FXML private StackPane rootLayout;
    @FXML private VBox dialogContainer;
    @FXML private GridPane formGrid;

    // Groups
    @FXML private VBox goalTypeGroup;
    @FXML private VBox activityLevelGroup;
    @FXML private VBox goalWeightGroup;
    @FXML private VBox weeklyGoalGroup;

    // Fields
    @FXML private ComboBox<WeightGoal> goalTypeComboBox;
    @FXML private ComboBox<ActivityLevel> activityLevelComboBox;
    @FXML private TextField goalWeightField;
    @FXML private ComboBox<Double> weeklyGoalComboBox;

    // Messages
    @FXML private Label goalTypeMessage;
    @FXML private Label activityLevelMessage;
    @FXML private Label goalWeightMessage;
    @FXML private Label weeklyGoalMessage;
    @FXML private Label actionMessage;

    // Buttons
    @FXML private Button saveButton;

    // Data
    private Double currentWeight;

    // Actions
    private Runnable onCancelAction;
    private Consumer<NutritionGoalUpdateRequest> onSaveAction;

    // Responsive
    private static final int NARROW_BREAKPOINT = 440;
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");

    private static final PseudoClass NO_SELECTION = PseudoClass.getPseudoClass("no-selection");

    private boolean narrowLayout;
    private Consumer<Boolean> onNarrowLayoutChanged;

    // ── Initialization ──────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize controls and Listeners
        initializeControls();
        addListeners();

        setWideLayout();

        SceneShortcuts.forNode(rootLayout)
                .onEscape(this::handleCancel)
                .onEnter(this::handleSave);
    }

    private void initializeControls() {
        goalTypeComboBox.getItems().setAll(WeightGoal.values());
        activityLevelComboBox.getItems().setAll(ActivityLevel.values());

        weeklyGoalComboBox.getItems().setAll(
                0.25,
                0.5,
                0.75,
                1.0
        );

        weeklyGoalComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Double value) {
                if (value == null) {
                    return "";
                }

                return NumberUtils.formatInputDecimal(value) + " kg / week";
            }

            @Override
            public Double fromString(String string) {
                return null;
            }
        });

        updateSelectionPseudoClass(goalTypeComboBox);
        updateSelectionPseudoClass(activityLevelComboBox);
        updateSelectionPseudoClass(weeklyGoalComboBox);
    }

    public void initializeResponsiveLayout(Region observedRegion) {
        initializeResponsiveWidthLayout(observedRegion, NARROW_BREAKPOINT);
    }

    private void addListeners() {
        goalTypeComboBox.valueProperty().addListener(
                (obs, oldValue, newValue) -> {
                    updateSelectionPseudoClass(goalTypeComboBox);

                    clearGoalTypeError();
                    updateGoalFieldsVisibility();
                }
        );

        activityLevelComboBox.valueProperty().addListener(
                (obs, oldValue, newValue) -> {
                    updateSelectionPseudoClass(activityLevelComboBox);

                    clearActivityLevelError();
                }
        );

        goalWeightField.textProperty().addListener(
                (obs, oldValue, newValue) ->
                        clearGoalWeightError()
        );

        weeklyGoalComboBox.valueProperty().addListener(
                (obs, oldValue, newValue) -> {
                    updateSelectionPseudoClass(weeklyGoalComboBox);

                    clearWeeklyGoalError();
                }
        );
    }

    // ── Configuration ───────────────────────────────────────
    public void setData(ProfileData profile) {
        currentWeight = profile.currentWeight();

        goalTypeComboBox.setValue(profile.goalType());
        activityLevelComboBox.setValue(profile.activityLevel());

        goalWeightField.setText(
                profile.goalWeight() != null
                        ? NumberUtils.formatInputDecimal(profile.goalWeight())
                        : ""
        );

        weeklyGoalComboBox.setValue(profile.weeklyGoal());

        updateGoalFieldsVisibility();
        clearValidationErrors();

        clearActionError();
    }

    public void setOnCancelAction(Runnable onCancelAction) {
        this.onCancelAction = onCancelAction;
    }

    public void setOnSaveAction(Consumer<NutritionGoalUpdateRequest> onSaveAction) {
        this.onSaveAction = onSaveAction;
    }

    public void setOnNarrowLayoutChanged(Consumer<Boolean> onNarrowLayoutChanged) {
        this.onNarrowLayoutChanged = onNarrowLayoutChanged;

        if (onNarrowLayoutChanged != null) {
            onNarrowLayoutChanged.accept(narrowLayout);
        }
    }

    // ── Responsive ──────────────────────────────────────────
    @Override
    public void updateWidthLayout(boolean narrow) {
        narrowLayout = narrow;

        rootLayout.pseudoClassStateChanged(NARROW, narrow);

        if (narrow) {
            setNarrowLayout();

            dialogContainer.prefWidthProperty().bind(rootLayout.widthProperty());
        } else {
            setWideLayout();

            dialogContainer.prefWidthProperty().unbind();
            dialogContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);
        }

        if (onNarrowLayoutChanged != null) {
            onNarrowLayoutChanged.accept(narrow);
        }
    }

    private void setWideLayout() {
        configureColumns(2);

        GridPane.setConstraints(
                goalTypeGroup,
                0, 0, 1, 1
        );

        GridPane.setConstraints(
                activityLevelGroup,
                1, 0, 1, 1
        );

        GridPane.setConstraints(
                goalWeightGroup,
                0, 1, 1, 1
        );

        GridPane.setConstraints(
                weeklyGoalGroup,
                1, 1, 1, 1
        );
    }

    private void setNarrowLayout() {
        configureColumns(1);

        GridPane.setConstraints(
                goalTypeGroup,
                0, 0, 1, 1
        );

        GridPane.setConstraints(
                activityLevelGroup,
                0, 1, 1, 1
        );

        GridPane.setConstraints(
                goalWeightGroup,
                0, 2, 1, 1
        );

        GridPane.setConstraints(
                weeklyGoalGroup,
                0, 3, 1, 1
        );
    }

    private void configureColumns(int count) {
        formGrid.getColumnConstraints().clear();

        for (int i = 0; i < count; i++) {
            ColumnConstraints column = new ColumnConstraints();

            column.setPercentWidth(
                    100.0 / count
            );

            column.setHgrow(Priority.ALWAYS);

            column.setFillWidth(true);

            formGrid.getColumnConstraints().add(column);
        }
    }

    // ── Button Actions ──────────────────────────────────────
    @FXML
    private void handleCancel() {
        if (isLoading(saveButton)) {
            return;
        }

        if (onCancelAction != null) {
            onCancelAction.run();
        }
    }

    @FXML
    private void handleSave() {
        if (isLoading(saveButton)) {
            return;
        }

        clearActionError();

        if (!isFormValid()) {
            return;
        }

        ActivityLevel activityLevel = activityLevelComboBox.getSelectionModel().getSelectedItem();
        WeightGoal goalType = goalTypeComboBox.getSelectionModel().getSelectedItem();

        Double goalWeight = null;
        Double weeklyGoal = null;

        if (goalType != WeightGoal.MAINTAIN_WEIGHT) {
            String goalWeightText = goalWeightField.getText().trim();

            goalWeight = goalWeightText.isEmpty()
                            ? null
                            : NumberUtils.parseDecimal(
                            goalWeightText
            );
            weeklyGoal = weeklyGoalComboBox.getSelectionModel().getSelectedItem();
        }

        NutritionGoalUpdateRequest request =
                new NutritionGoalUpdateRequest(
                        activityLevel.name(),
                        goalType.name(),
                        goalWeight,
                        weeklyGoal
                );

        if (onSaveAction != null) {
            onSaveAction.accept(request);
        }
    }

    // ── Validation ──────────────────────────────────────────
    private boolean isFormValid() {
        ActivityLevel activityLevel = activityLevelComboBox.getSelectionModel().getSelectedItem();
        WeightGoal goalType = goalTypeComboBox.getSelectionModel().getSelectedItem();

        boolean valid = true;

        if (goalType == null) {
            showGoalTypeMessage();
            shake(goalTypeComboBox);
            valid = false;
        }

        if (activityLevel == null) {
            showActivityLevelMessage();
            shake(activityLevelComboBox);
            valid = false;
        }

        if (goalType == WeightGoal.LOSE_WEIGHT || goalType == WeightGoal.GAIN_WEIGHT) {
            if (!validateGoalWeight(goalType)) {
                shake(goalWeightField);
                valid = false;
            }

            if (weeklyGoalComboBox.getSelectionModel().getSelectedItem() == null) {
                showWeeklyGoalMessage();
                shake(weeklyGoalComboBox);
                valid = false;
            }
        }

        return valid;
    }

    private boolean validateGoalWeight(WeightGoal goalType) {
        String goalWeightText = goalWeightField.getText().trim();

        // Goal weight is optional
        if (goalWeightText.isEmpty()) {
            clearGoalWeightError();
            return true;
        }

        if (!FitnessInputValidator.isWeightValid(goalWeightText)) {
            showGoalWeightMessage(AppConstants.Messages.INVALID_WEIGHT_MESSAGE);
            return false;
        }

        double goalWeight = NumberUtils.parseDecimal(goalWeightText);

        if (currentWeight == null) {
            return true;
        }

        if (goalType == WeightGoal.LOSE_WEIGHT && goalWeight >= currentWeight) {
            showGoalWeightMessage(AppConstants.Messages.INVALID_GOAL_WEIGHT_LOSE_MESSAGE);
            return false;
        }

        if (goalType == WeightGoal.GAIN_WEIGHT && goalWeight <= currentWeight) {
            showGoalWeightMessage(AppConstants.Messages.INVALID_GOAL_WEIGHT_GAIN_MESSAGE);
            return false;
        }

        clearGoalWeightError();
        return true;
    }

    // ── Save State ──────────────────────────────────────────
    public void setSaving(boolean saving) {
        if (saving) {
            setLoading(saveButton, "Saving...");
        } else {
            resetLoading(saveButton);
        }
    }

    // ── Goal Type Helpers ───────────────────────────────────
    private void showGoalTypeMessage() {
        setFieldMessage(goalTypeMessage, AppConstants.Messages.GOAL_NOT_SELECTED_MESSAGE, true, goalTypeComboBox);
    }

    private void clearGoalTypeError() {
        clearFieldMessage(goalTypeMessage, goalTypeComboBox);
    }

    // ── Activity Level Helpers ──────────────────────────────
    private void showActivityLevelMessage() {
        setFieldMessage(activityLevelMessage, AppConstants.Messages.ACTIVITY_NOT_SELECTED_MESSAGE, true, activityLevelComboBox);
    }

    private void clearActivityLevelError() {
        clearFieldMessage(activityLevelMessage, activityLevelComboBox);
    }

    // ── Goal Weight Helpers ─────────────────────────────────
    private void showGoalWeightMessage(String message) {
        setFieldMessage(goalWeightMessage, message, true, goalWeightField);
    }

    private void clearGoalWeightError() {
        clearFieldMessage(goalWeightMessage, goalWeightField);
    }

    // ── Weekly Goal Helpers ─────────────────────────────────
    private void showWeeklyGoalMessage() {
        setFieldMessage(weeklyGoalMessage, AppConstants.Messages.WEEKLY_GOAL_NOT_SELECTED_MESSAGE, true, weeklyGoalComboBox);
    }

    private void clearWeeklyGoalError() {
        clearFieldMessage(weeklyGoalMessage, weeklyGoalComboBox);
    }

    // ── Save message Helpers ─────────────────────────────────────────────────
    public void showActionError(String message) {
        setFormMessage(actionMessage, message, true);
    }

    private void clearActionError() {
        clearFormMessage(actionMessage);
    }

    // ── Goal Fields Helpers ─────────────────────────────────
    private void updateGoalFieldsVisibility() {
        WeightGoal goalType = goalTypeComboBox.getSelectionModel().getSelectedItem();

        boolean visible =
                goalType == WeightGoal.LOSE_WEIGHT
                        || goalType == WeightGoal.GAIN_WEIGHT;

        setVisible(goalWeightGroup, visible);
        setVisible(weeklyGoalGroup, visible);

        if (!visible) {
            clearGoalWeightError();
            clearWeeklyGoalError();
        }
    }

    private void clearValidationErrors() {
        clearGoalTypeError();
        clearActivityLevelError();
        clearGoalWeightError();
        clearWeeklyGoalError();
    }

    private void updateSelectionPseudoClass(ComboBox<?> comboBox) {
        comboBox.pseudoClassStateChanged(
                NO_SELECTION,
                comboBox.getValue() == null
        );
    }
}