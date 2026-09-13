package com.fittrack.controller.nutrition.components;

import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.model.nutrition.MealCopyMode;
import com.fittrack.model.nutrition.MealType;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

public class MealCopyDialogController implements Initializable, ResponsiveLayout {

    @FXML private StackPane rootLayout;
    @FXML private Label titleLabel;
    @FXML private ComboBox<MealType> mealComboBox;
    @FXML private GridPane fieldsGrid;
    @FXML private VBox mealFieldBox;
    @FXML private VBox dayFieldBox;
    @FXML private DatePicker datePicker;
    @FXML private Label availabilityMessage;
    @FXML private Button copyButton;

    // Mode
    private MealCopyMode mode;

    // Current Meal
    private MealType currentMealType;
    private LocalDate currentDate;

    private Runnable onCloseAction;
    private BiConsumer<MealType, LocalDate> onAvailabilityCheckAction;
    private BiConsumer<MealType, LocalDate> onCopyAction;

    // Date Format
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.uuuu");

    // Narrow
    private static final int NARROW_BREAKPOINT = 460;
    private static final int ULTRA_NARROW_BREAKPOINT = 340;
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");
    private static final PseudoClass ULTRA_NARROW = PseudoClass.getPseudoClass("ultra-narrow");

    // ── Initialize ─────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize controls
        initializeCopyDialogControls();

        // Initialize Responsive
        initializeResponsiveWidthLayout(rootLayout, NARROW_BREAKPOINT);

        // Add listeners
        addListeners();
    }

    // ── Initialize Helpers ─────────────────────────────────────────────────
    private void addListeners() {
        rootLayout.widthProperty().addListener(
                (obs, oldWidth, newWidth) ->
                        updateUltraNarrowLayout(
                                newWidth.doubleValue() <= ULTRA_NARROW_BREAKPOINT
                        )
        );

        mealComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) ->
                        handleSelectionChanged()
        );
    }

    private void initializeCopyDialogControls() {
        datePicker.setEditable(false);

        datePicker.setConverter(new StringConverter<>() {

            @Override
            public String toString(LocalDate date) {
                if (date == null) {
                    return "";
                }

                return date.format(DATE_FORMATTER);
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) {
                    return datePicker.getValue();
                }

                try {
                    return LocalDate.parse(
                            text.trim(),
                            DATE_FORMATTER
                    );
                } catch (DateTimeParseException _) {
                    return datePicker.getValue();
                }
            }
        });

        datePicker.valueProperty().addListener((observable, oldDate, newDate) -> {
            if (newDate != null) {
                handleSelectionChanged();
            }
        });

        datePicker.setValue(LocalDate.now());
    }

    // ── Responsive Helpers ─────────────────────────────────────────────────
    @Override
    public void updateWidthLayout(boolean narrow) {
        rootLayout.pseudoClassStateChanged(NARROW, narrow);
    }

    private void updateUltraNarrowLayout(boolean ultraNarrow) {
        rootLayout.pseudoClassStateChanged(ULTRA_NARROW, ultraNarrow);

        if (ultraNarrow) {
            GridPane.setColumnIndex(mealFieldBox, 0);
            GridPane.setRowIndex(mealFieldBox, 0);
            GridPane.setColumnSpan(mealFieldBox, 2);

            GridPane.setColumnIndex(dayFieldBox, 0);
            GridPane.setRowIndex(dayFieldBox, 1);
            GridPane.setColumnSpan(dayFieldBox, 2);
        } else {
            GridPane.setColumnIndex(mealFieldBox, 0);
            GridPane.setRowIndex(mealFieldBox, 0);
            GridPane.setColumnSpan(mealFieldBox, 1);

            GridPane.setColumnIndex(dayFieldBox, 1);
            GridPane.setRowIndex(dayFieldBox, 0);
            GridPane.setColumnSpan(dayFieldBox, 1);
        }
    }

    // ── Responsive Helpers ─────────────────────────────────────────────────
    private void handleSelectionChanged() {
        if (mode == null) {
            return;
        }

        MealType selectedMealType = mealComboBox.getValue();
        LocalDate selectedDate = datePicker.getValue();

        if (selectedMealType == null || selectedDate == null) {
            availabilityMessage.setVisible(false);
            copyButton.setDisable(true);
            return;
        }

        if (isCurrentMeal(selectedMealType, selectedDate)) {
            copyButton.setDisable(true);

            if (mode == MealCopyMode.FROM) {
                availabilityMessage.setText(
                        "Choose a different meal or day."
                );
                availabilityMessage.setVisible(true);
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

        availabilityMessage.setManaged(true);
        availabilityMessage.setVisible(false);

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

        availabilityMessage.setVisible(false);

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
        availabilityMessage.setVisible(false);
        copyButton.setDisable(true);
    }

    public void setMealAvailable() {
        availabilityMessage.setVisible(false);
        copyButton.setDisable(false);
    }

    public void setMealUnavailable() {
        availabilityMessage.setText("No food logged for that meal.");
        availabilityMessage.setVisible(true);
        copyButton.setDisable(true);
    }

    public void setAvailabilityCheckFailed() {
        availabilityMessage.setText("Could not check that meal.");
        availabilityMessage.setVisible(true);
        copyButton.setDisable(true);
    }

    // ── Helpers ─────────────────────────────────────────────
    private boolean isCurrentMeal(MealType mealType, LocalDate date) {
        return mealType == currentMealType
                && date.equals(currentDate);
    }
}
