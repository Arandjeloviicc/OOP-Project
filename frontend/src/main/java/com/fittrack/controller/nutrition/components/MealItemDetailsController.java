package com.fittrack.controller.nutrition.components;

import com.fittrack.controller.common.FormController;
import com.fittrack.dto.nutrition.FoodResponse;
import com.fittrack.model.nutrition.MealType;
import com.fittrack.util.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class MealItemDetailsController extends FormController implements Initializable {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(MealItemDetailsController.class);

    @Override
    protected Logger getLogger() { return log; }

    @FXML private Label captionLabel;
    @FXML private Label selectedFoodTitleLabel;
    @FXML private ComboBox<MealType> mealComboBox;
    @FXML private HBox servingRow;
    @FXML private VBox servingSizeContainer;
    @FXML private VBox servingsContainer;
    @FXML private ComboBox<String> servingSizeComboBox;
    @FXML private TextField servingsField;
    @FXML private Label servingsMessage;
    @FXML private VBox nutrientPreview;
    @FXML private Button confirmButton;

    // Helpers
    private FoodResponse selectedFood;
    private NutritionMacroPreviewController nutritionMacroPreviewController;

    private Runnable onCancelAction;
    private Consumer<Double> onConfirmAction;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize Serving Fields
        initializeServingLayout();

        // Listeners
        addListeners();
    }

    // ── Set Data ─────────────────────────────────────────────────
    public void setData(FoodResponse food, MealType mealType) {
        selectedFood = food;

        selectedFoodTitleLabel.setText(food.name());

        mealComboBox.getItems().setAll(MealType.values());
        mealComboBox.setValue(mealType);

        servingSizeComboBox.getItems().setAll(
                Math.round(food.servingSizeGrams()) + " g"
        );

        servingSizeComboBox.getSelectionModel().selectFirst();

        servingsField.setText("1");

        LoadedComponent<NutritionMacroPreviewController> nutrients = FxmlComponentLoader.load(AppConstants.Components.NUTRITION_MACRO_PREVIEW);

        nutritionMacroPreviewController = nutrients.controller();

        nutrientPreview.getChildren().setAll(nutrients.root());

        updateNutrientsInfo();
    }

    // ── Callback Setters ─────────────────────────────────────────────────
    public void setOnCancelAction(Runnable onCancelAction) {
        this.onCancelAction = onCancelAction;
    }

    public void setOnConfirmAction(Consumer<Double> onConfirmAction) {
        this.onConfirmAction = onConfirmAction;
    }

    // ── Action Setters ─────────────────────────────────────────────────
    public void setCaption(String text) {
        captionLabel.setText(text);
    }

    public void setConfirmButtonText(String text) {
        confirmButton.setText(text);
    }

    // ── Button Actions ─────────────────────────────────────────────────
    @FXML
    private void handleCancel() {
        if (onCancelAction != null) {
            onCancelAction.run();
        }
    }

    @FXML
    private void handleConfirm() {
        String servingsText = servingsField.getText();

        if (servingsText.isEmpty()) {
            showServingsMessage();
            shake(servingsField);
            return;
        }

        double numberOfServings;

        try {
            numberOfServings = NumberUtils.parseDecimal(servingsText);
        } catch (NumberFormatException _) {
            showServingsMessage();
            shake(servingsField);
            return;
        }

        if (numberOfServings <= 0) {
            showServingsMessage();
            shake(servingsField);
            return;
        }

        double servingSizeGrams = getSelectedServingSizeGrams();
        double quantityGrams = servingSizeGrams * numberOfServings;

        if (onConfirmAction != null) {
            onConfirmAction.accept(quantityGrams);
        }
    }

    // ── Initialize Helpers ─────────────────────────────────────────────────
    private void initializeServingLayout() {
        servingSizeContainer.prefWidthProperty().bind(
                servingRow.widthProperty()
                        .subtract(servingRow.getSpacing())
                        .multiply(0.4)
        );

        servingsContainer.prefWidthProperty().bind(
                servingRow.widthProperty()
                        .subtract(servingRow.getSpacing())
                        .multiply(0.6)
        );
    }

    private void addListeners() {
        TextFieldValidators.applyDecimalFilter(servingsField);

        servingsField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateNutrientsInfo();
                    restoreServingsHelper();
                }
        );

        servingSizeComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) ->
                        updateNutrientsInfo()
        );

        restoreServingsHelper();
    }

    private void updateNutrientsInfo() {
        if (selectedFood == null
                || nutritionMacroPreviewController == null
                || servingSizeComboBox.getValue() == null) {
            return;
        }

        double numberOfServings;
        try {
            numberOfServings = NumberUtils.parseDecimal(servingsField.getText());
        } catch (NumberFormatException _) {
            return;
        }

        if (numberOfServings <= 0) {
            return;
        }

        double selectedServingSize = getSelectedServingSizeGrams();
        double baseServingSize = selectedFood.servingSizeGrams();

        if (baseServingSize <= 0) {
            return;
        }

        double quantityGrams =
                selectedServingSize * numberOfServings;

        double ratio = quantityGrams / baseServingSize;

        double calories = selectedFood.caloriesPerServing() * ratio;
        double carbs = selectedFood.carbsPerServing() * ratio;
        double fat = selectedFood.fatPerServing() * ratio;
        double protein = selectedFood.proteinPerServing() * ratio;

        nutritionMacroPreviewController.setData(calories, carbs, fat, protein);
    }

    // ── Meal Helpers ─────────────────────────────────────────────────
    public MealType getSelectedMealType() {
        return mealComboBox.getValue();
    }

    // ── Servings Helpers ─────────────────────────────────────────────────
    private double getSelectedServingSizeGrams() {
        return selectedFood.servingSizeGrams();
    }

    private void showServingsMessage() {
        setFieldMessage(servingsMessage, AppConstants.Messages.INVALID_SERVINGS_MESSAGE, true, servingsField);
    }

    private void restoreServingsHelper() {
        setFieldMessage(servingsMessage, "", false, servingsField);
    }
}