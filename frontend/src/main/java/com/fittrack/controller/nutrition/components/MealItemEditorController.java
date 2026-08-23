package com.fittrack.controller.nutrition.components;

import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.FormController;
import com.fittrack.dto.nutrition.meal.item.MealItemDraft;
import com.fittrack.dto.nutrition.food.FoodResponse;
import com.fittrack.dto.nutrition.meal.item.MealItemResponse;
import com.fittrack.model.nutrition.MealType;
import com.fittrack.ui.FxmlComponentLoader;
import com.fittrack.ui.LoadedComponent;
import com.fittrack.ui.SceneShortcuts;
import com.fittrack.ui.TextFieldValidators;
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

public class MealItemEditorController extends FormController implements Initializable {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(MealItemEditorController.class);

    @Override
    protected Logger getLogger() { return log; }

    @FXML private VBox rootLayout;

    @FXML private Label captionLabel;
    @FXML private Label selectedFoodTitleLabel;
    @FXML private VBox mealContainer;
    @FXML private ComboBox<MealType> mealComboBox;
    @FXML private HBox servingRow;
    @FXML private VBox servingSizeContainer;
    @FXML private VBox servingsContainer;
    @FXML private ComboBox<String> servingSizeComboBox;
    @FXML private TextField servingsField;
    @FXML private Label servingsMessage;
    @FXML private VBox nutrientPreview;
    @FXML private Button confirmButton;
    @FXML private Button removeButton;

    // Helpers
    private FoodResponse selectedFood;
    private MealItemResponse selectedMealItem;
    private MealItemDraft selectedDraftItem;
    private NutritionMacroPreviewController nutritionMacroPreviewController;

    private Runnable onCancelAction;
    private Consumer<Double> onConfirmAction;
    private Runnable onRemoveAction;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize Serving Fields
        initializeServingLayout();

        // Keyboard Shortcuts
        SceneShortcuts.forNode(rootLayout)
                .onEscape(this::handleCancel)
                .onEnter(this::handleConfirm);

        // Listeners
        addListeners();
    }

    // ── Set Data ─────────────────────────────────────────────────
    public void setData(FoodResponse food, MealType mealType) {
        selectedFood = food;
        selectedMealItem = null;
        selectedDraftItem = null;

        selectedFoodTitleLabel.setText(food.name());

        setVisible(mealContainer, true);
        mealComboBox.getItems().setAll(MealType.values());
        mealComboBox.setValue(mealType);

        servingSizeComboBox.getItems().setAll(
                Math.round(food.servingSizeGrams()) + " g"
        );

        servingSizeComboBox.getSelectionModel().selectFirst();

        servingsField.setText("1");

        setVisible(removeButton, false);

        // Macro Preview
        loadNutritionMacroPreview();
    }

    public void setData(MealItemResponse mealItem, MealType mealType) {
        selectedMealItem = mealItem;
        selectedFood = null;
        selectedDraftItem = null;

        selectedFoodTitleLabel.setText(mealItem.foodName());

        setVisible(mealContainer, true);
        mealComboBox.getItems().setAll(MealType.values());
        mealComboBox.setValue(mealType);

        servingSizeComboBox.getItems().setAll(
                Math.round(mealItem.servingSizeGrams()) + " g"
        );

        servingSizeComboBox.getSelectionModel().selectFirst();

        double numberOfServings = mealItem.quantityGrams() / mealItem.servingSizeGrams();

        servingsField.setText(NumberUtils.formatDecimal(numberOfServings));

        setVisible(removeButton, true);

        // Macro Preview
        loadNutritionMacroPreview();
    }

    public void setData(MealItemDraft item) {
        selectedDraftItem = item;
        selectedFood = null;
        selectedMealItem = null;

        selectedFoodTitleLabel.setText(item.foodName());

        setVisible(mealContainer, false);

        servingSizeComboBox.getItems().setAll(
                Math.round(item.servingSizeGrams()) + " g"
        );

        servingSizeComboBox.getSelectionModel().selectFirst();

        double numberOfServings = item.quantityGrams() / item.servingSizeGrams();

        servingsField.setText(NumberUtils.formatDecimal(numberOfServings));

        setVisible(removeButton, true);

        loadNutritionMacroPreview();
    }

    public void setDraftData(FoodResponse food) {
        selectedFood = food;
        selectedMealItem = null;
        selectedDraftItem = null;

        selectedFoodTitleLabel.setText(food.name());

        servingSizeComboBox.getItems().setAll(
                Math.round(food.servingSizeGrams()) + " g"
        );

        servingSizeComboBox.getSelectionModel().selectFirst();

        servingsField.setText("1");

        setVisible(mealContainer, false);
        setVisible(removeButton, false);

        loadNutritionMacroPreview();
    }

    // ── Callback Setters ─────────────────────────────────────────────────
    public void setOnCancelAction(Runnable onCancelAction) {
        this.onCancelAction = onCancelAction;
    }

    public void setOnConfirmAction(Consumer<Double> onConfirmAction) {
        this.onConfirmAction = onConfirmAction;
    }

    public void setOnRemoveAction(Runnable onRemoveAction) {
        this.onRemoveAction = onRemoveAction;
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

    @FXML
    private void handleRemove() {
        if (onRemoveAction != null) {
            onRemoveAction.run();
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

    // ── Nutrient Macro Preview Helpers ─────────────────────────────────────────────────
    private void loadNutritionMacroPreview() {
        LoadedComponent<NutritionMacroPreviewController> nutrients = FxmlComponentLoader.load(AppConstants.Components.NUTRITION_MACRO_PREVIEW);

        nutritionMacroPreviewController = nutrients.controller();

        nutrientPreview.getChildren().setAll(nutrients.root());

        updateNutrientsInfo();
    }

    private void updateNutrientsInfo() {
        if ((selectedFood == null
                && selectedMealItem == null
                && selectedDraftItem == null)
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

        double baseServingSize;
        double caloriesPerServing;
        double carbsPerServing;
        double fatPerServing;
        double proteinPerServing;

        if (selectedFood != null) {
            baseServingSize = selectedFood.servingSizeGrams();
            caloriesPerServing = selectedFood.caloriesPerServing();
            carbsPerServing = selectedFood.carbsPerServing();
            fatPerServing = selectedFood.fatPerServing();
            proteinPerServing = selectedFood.proteinPerServing();

        } else if (selectedMealItem != null) {
            baseServingSize = selectedMealItem.servingSizeGrams();
            caloriesPerServing = selectedMealItem.caloriesPerServing();
            carbsPerServing = selectedMealItem.carbsPerServing();
            fatPerServing = selectedMealItem.fatPerServing();
            proteinPerServing = selectedMealItem.proteinPerServing();

        } else {
            baseServingSize = selectedDraftItem.servingSizeGrams();
            caloriesPerServing = selectedDraftItem.caloriesPerServing();
            carbsPerServing = selectedDraftItem.carbsPerServing();
            fatPerServing = selectedDraftItem.fatPerServing();
            proteinPerServing = selectedDraftItem.proteinPerServing();
        }

        if (baseServingSize <= 0) {
            return;
        }

        double quantityGrams = selectedServingSize * numberOfServings;

        double ratio = quantityGrams / baseServingSize;

        double calories = caloriesPerServing * ratio;
        double carbs = carbsPerServing * ratio;
        double fat = fatPerServing * ratio;
        double protein = proteinPerServing * ratio;

        nutritionMacroPreviewController.setData(calories, carbs, fat, protein);
    }

    // ── Meal Helpers ─────────────────────────────────────────────────
    public MealType getSelectedMealType() {
        return mealComboBox.getValue();
    }

    // ── Servings Helpers ─────────────────────────────────────────────────
    private double getSelectedServingSizeGrams() {
        if (selectedFood != null) {
            return selectedFood.servingSizeGrams();
        }

        if (selectedMealItem != null) {
            return selectedMealItem.servingSizeGrams();
        }

        return selectedDraftItem.servingSizeGrams();
    }

    private void showServingsMessage() {
        setFieldMessage(servingsMessage, AppConstants.Messages.INVALID_SERVINGS_MESSAGE, true, servingsField);
    }

    private void restoreServingsHelper() {
        setFieldMessage(servingsMessage, "", false, servingsField);
    }
}