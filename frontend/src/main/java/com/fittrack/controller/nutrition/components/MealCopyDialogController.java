package com.fittrack.controller.nutrition.components;

import com.fittrack.model.nutrition.MealCopyMode;
import com.fittrack.model.nutrition.MealType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

public class MealCopyDialogController implements Initializable {

    @FXML private StackPane rootLayout;
    @FXML private Label titleLabel;
    @FXML private ComboBox<MealType> mealComboBox;
    @FXML private DatePicker datePicker;
    @FXML private Label availabilityLabel;
    @FXML private Button copyButton;

    // Mode
    private MealCopyMode mode;

    // Current Meal
    private MealType currentMealType;
    private LocalDate currentDate;

    private Runnable onCloseAction;
    private BiConsumer<MealType, LocalDate> onAvailabilityCheckAction;
    private BiConsumer<MealType, LocalDate> onCopyAction;

    // ── Initialize ─────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addListeners();
    }

    private void addListeners() {
        mealComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) ->
                        handleSelectionChanged()
        );

        datePicker.valueProperty().addListener(
                (observable, oldValue, newValue) ->
                        handleSelectionChanged()
        );
    }

    private void handleSelectionChanged() {
        if (mode == null) {
            return;
        }

        MealType selectedMealType = mealComboBox.getValue();
        LocalDate selectedDate = datePicker.getValue();

        if (selectedMealType == null || selectedDate == null) {
            availabilityLabel.setVisible(false);
            copyButton.setDisable(true);
            return;
        }

        if (isCurrentMeal(selectedMealType, selectedDate)) {
            copyButton.setDisable(true);

            if (mode == MealCopyMode.FROM) {
                availabilityLabel.setText(
                        "Choose a different meal or day."
                );
                availabilityLabel.setVisible(true);
            }

            return;
        }

        if (mode == MealCopyMode.FROM) {
            setCheckingAvailability();

            if (onAvailabilityCheckAction  != null) {
                onAvailabilityCheckAction.accept(
                        selectedMealType,
                        selectedDate
                );
            }
        } else {
            copyButton.setDisable(false);
        }
    }

    // ── Configure ─────────────────────────────────────────────
    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    public void setOnAvailabilityCheckAction(BiConsumer<MealType, LocalDate> onAvailabilityCheckAction) {
        this.onAvailabilityCheckAction = onAvailabilityCheckAction;
    }

    public void setOnCopyAction(BiConsumer<MealType, LocalDate> onCopyAction) {
        this.onCopyAction = onCopyAction;
    }

    public void setCopyFrom(MealType currentMealType, LocalDate currentDate) {
        mode = null;

        this.currentMealType = currentMealType;
        this.currentDate = currentDate;

        titleLabel.setText("Copy from");

        mealComboBox.getItems().setAll(MealType.values());
        mealComboBox.setValue(currentMealType);

        datePicker.setValue(currentDate.minusDays(1));

        availabilityLabel.setManaged(true);
        availabilityLabel.setVisible(false);

        copyButton.setDisable(true);

        mode = MealCopyMode.FROM;

        handleSelectionChanged();
    }

    public void setCopyTo(MealType currentMealType, LocalDate currentDate) {
        mode = null;

        this.currentMealType = currentMealType;
        this.currentDate = currentDate;

        titleLabel.setText("Copy to");

        mealComboBox.getItems().setAll(MealType.values());
        mealComboBox.setValue(currentMealType);

        datePicker.setValue(currentDate.plusDays(1));

        availabilityLabel.setVisible(false);
        availabilityLabel.setManaged(false);

        mode = MealCopyMode.TO;

        handleSelectionChanged();
    }

    // ── Button Actions ─────────────────────────────────────────────
    @FXML
    private void handleClose() {
        if (onCloseAction != null) {
            onCloseAction.run();
        }
    }

    @FXML
    private void handleCopy() {
        MealType selectedMealType = mealComboBox.getValue();
        LocalDate selectedDate = datePicker.getValue();

        if (selectedMealType == null || selectedDate == null) {
            return;
        }

        if (isCurrentMeal(selectedMealType, selectedDate)) {
            return;
        }

        if (onCopyAction != null) {
            onCopyAction.accept(selectedMealType, selectedDate);
        }
    }

    // ── Availability ─────────────────────────────────────────────
    public void setCheckingAvailability() {
        availabilityLabel.setVisible(false);
        copyButton.setDisable(true);
    }

    public void setMealAvailable() {
        availabilityLabel.setVisible(false);
        copyButton.setDisable(false);
    }

    public void setMealUnavailable() {
        availabilityLabel.setText("No food logged for that meal.");
        availabilityLabel.setVisible(true);
        copyButton.setDisable(true);
    }

    public void setAvailabilityCheckFailed() {
        availabilityLabel.setText("Could not check that meal.");
        availabilityLabel.setVisible(true);
        copyButton.setDisable(true);
    }

    // ── Helpers ─────────────────────────────────────────────
    private boolean isCurrentMeal(MealType mealType, LocalDate date) {
        return mealType == currentMealType
                && date.equals(currentDate);
    }
}
