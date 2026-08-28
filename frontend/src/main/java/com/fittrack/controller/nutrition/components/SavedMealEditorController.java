package com.fittrack.controller.nutrition.components;

import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.FormController;
import com.fittrack.controller.common.components.DeleteConfirmationController;
import com.fittrack.dto.nutrition.meal.CreateMealRequest;
import com.fittrack.dto.nutrition.meal.MealResponse;
import com.fittrack.dto.nutrition.meal.UpdateSavedMealRequest;
import com.fittrack.dto.nutrition.meal.item.*;
import com.fittrack.model.nutrition.SavedMealEditorMode;
import com.fittrack.ui.FxmlComponentLoader;
import com.fittrack.ui.LoadedComponent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class SavedMealEditorController extends FormController implements Initializable {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(SavedMealEditorController.class);

    @Override
    protected Logger getLogger() { return log; }

    @FXML private StackPane rootLayout;
    @FXML private VBox editorContainer;

    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private Label nameMessage;

    @FXML private VBox macroPreviewContainer;

    @FXML private Label itemCountLabel;
    @FXML private VBox itemsContainer;

    @FXML private Button saveButton;
    @FXML private Button deleteMealButton;

    @FXML private StackPane itemDetailsContainer;

    @FXML private StackPane confirmationContainer;

    // Current Meal that is edited
    private Integer mealId;

    // Mode (Create/Edit)
    private SavedMealEditorMode mode = SavedMealEditorMode.CREATE;

    // Macro Preview
    private NutritionMacroPreviewController macroPreview;

    // Added items
    private final List<MealItemDraft> draftItems = new ArrayList<>();

    // Original Edit State
    private String originalName;
    private List<MealItemDraft> originalDraftItems = List.of();

    // Actions
    private Runnable onCancelAction;
    private Runnable onAddFoodAction;
    private Consumer<CreateMealRequest> onCreateAction;
    private Consumer<UpdateSavedMealRequest> onUpdateAction;
    private Runnable onDeleteAction;

    // ── Initialization ─────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeMacroPreview();

        // Add listeners
        addListeners();

        // Default Mode is Create
        setCreateMode();
    }

    private void initializeMacroPreview() {
        LoadedComponent<NutritionMacroPreviewController> preview = FxmlComponentLoader.load(AppConstants.Components.NUTRITION_MACRO_PREVIEW);

        macroPreview = preview.controller();

        macroPreviewContainer.getChildren().setAll(preview.root());
    }

    private void addListeners() {
        nameField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    restoreNameHelper();
                    updateSaveButton();
                }
        );
        restoreNameHelper();
    }

    // ── Configuration ──────────────────────────────────────────
    public void setOnCancelAction(Runnable onCancelAction) {
        this.onCancelAction = onCancelAction;
    }

    public void setOnAddFoodAction(Runnable onAddFoodAction) {
        this.onAddFoodAction = onAddFoodAction;
    }

    public void setOnCreateAction(Consumer<CreateMealRequest> onCreateAction) {
        this.onCreateAction = onCreateAction;
    }

    public void setOnUpdateAction(Consumer<UpdateSavedMealRequest> onUpdateAction) {
        this.onUpdateAction = onUpdateAction;
    }

    public void setOnDeleteAction(Runnable onDeleteAction) {
        this.onDeleteAction = onDeleteAction;
    }

    public void setCreateMode() {
        mealId = null;

        mode = SavedMealEditorMode.CREATE;

        originalName = null;
        originalDraftItems = List.of();

        titleLabel.setText("Create Meal");
        saveButton.setText("Create meal");

        setVisible(deleteMealButton, false);

        nameField.clear();
        draftItems.clear();

        refreshDraft();
    }

    public void setCreateMode(MealResponse sourceMeal) {
        mealId = null;

        mode = SavedMealEditorMode.CREATE;

        originalName = null;
        originalDraftItems = List.of();

        titleLabel.setText("Save as My Meal");
        saveButton.setText("Save meal");

        setVisible(deleteMealButton, false);

        nameField.clear();
        draftItems.clear();

        for (MealItemResponse item : sourceMeal.items()) {
            draftItems.add(
                    new MealItemDraft(
                            null,
                            item.foodId(),
                            item.foodName(),
                            item.brand(),
                            item.quantityGrams(),
                            item.servingSizeGrams(),
                            item.caloriesPerServing(),
                            item.proteinPerServing(),
                            item.carbsPerServing(),
                            item.fatPerServing()
                    )
            );
        }

        refreshDraft();
    }

    public void setEditMode(MealResponse meal) {
        mealId = meal.id();

        mode = SavedMealEditorMode.EDIT;

        titleLabel.setText("Edit Meal");
        saveButton.setText("Save changes");

        setVisible(deleteMealButton, true);

        originalName = meal.name().trim();
        nameField.setText(originalName);

        draftItems.clear();

        for (MealItemResponse item : meal.items()) {
            draftItems.add(
                    new MealItemDraft(
                            item.id(),
                            item.foodId(),
                            item.foodName(),
                            item.brand(),
                            item.quantityGrams(),
                            item.servingSizeGrams(),
                            item.caloriesPerServing(),
                            item.proteinPerServing(),
                            item.carbsPerServing(),
                            item.fatPerServing()
                    )
            );
        }

        refreshDraft();
    }

    // ── Draft State ────────────────────────────────────────────
    private boolean hasChanges() {
        if (mode != SavedMealEditorMode.EDIT) {
            return true;
        }

        String currentName = nameField.getText().trim();

        return !currentName.equals(originalName)
                || !draftItems.equals(originalDraftItems);
    }

    public void addDraftItem(MealItemDraft item) {
        draftItems.add(item);
        refreshDraft();
    }

    public void addDraftItems(List<MealItemDraft> items) {
        draftItems.addAll(items);
        refreshDraft();
    }

    private void updateDraftItem(MealItemDraft currentItem, MealItemDraft updatedItem) {
        int index = draftItems.indexOf(currentItem);

        if (index == -1) {
            return;
        }

        draftItems.set(index, updatedItem);
        refreshDraft();
    }

    private void removeDraftItem(MealItemDraft item) {
        draftItems.remove(item);
        refreshDraft();
    }

    // ── Draft Refresh ──────────────────────────────────────────
    private void refreshDraft() {
        updateItems();
        updateMacroPreview();
        updateItemCount();
        updateSaveButton();
    }

    private void updateItems() {
        itemsContainer.getChildren().clear();

        for (MealItemDraft item : draftItems) {
            LoadedComponent<MealItemCardController> card = FxmlComponentLoader.load(AppConstants.Components.MEAL_ITEM_CARD);

            double servings = item.quantityGrams() / item.servingSizeGrams();
            double calories = item.caloriesPerServing() * servings;

            card.controller().setData(
                    item.foodName(),
                    item.quantityGrams(),
                    calories
            );

            card.controller().setOnOpenAction(
                    () -> openDraftItemDetails(item)
            );

            itemsContainer.getChildren().add(card.root());
        }
    }

    private void openDraftItemDetails(MealItemDraft item) {
        LoadedComponent<MealItemEditorController> details = FxmlComponentLoader.load(AppConstants.Components.MEAL_ITEM_EDITOR);

        details.controller().setData(item);
        details.controller().setCaption("Edit food");
        details.controller().setConfirmButtonText("Save changes");

        details.controller().setOnCancelAction(
                this::closeItemDetails
        );

        details.controller().setOnConfirmAction(quantityGrams -> {
            updateDraftItem(item, item.withQuantity(quantityGrams));

            closeItemDetails();
        });

        details.controller().setOnRemoveAction(() -> {
            removeDraftItem(item);
            closeItemDetails();
        });

        itemDetailsContainer.getChildren().setAll(details.root());

        setVisible(editorContainer, false);
        setVisible(itemDetailsContainer, true);
    }

    private void closeItemDetails() {
        itemDetailsContainer.getChildren().clear();

        setVisible(itemDetailsContainer, false);
        setVisible(editorContainer, true);
    }

    private void updateMacroPreview() {
        double calories = 0;
        double carbs = 0;
        double fat = 0;
        double protein = 0;

        for (MealItemDraft item : draftItems) {
            double servings = item.quantityGrams() / item.servingSizeGrams();

            calories += item.caloriesPerServing() * servings;
            carbs += item.carbsPerServing() * servings;
            fat += item.fatPerServing() * servings;
            protein += item.proteinPerServing() * servings;
        }

        macroPreview.setData(calories, carbs, fat, protein);
    }

    private void updateItemCount() {
        int count = draftItems.size();

        itemCountLabel.setText(
                count == 1
                        ? "1 item"
                        : count + " items"
        );
    }

    private void updateSaveButton() {
        boolean hasItems = !draftItems.isEmpty();

        saveButton.setVisible(hasItems);

        if (mode == SavedMealEditorMode.EDIT) {
            saveButton.setDisable(!hasChanges());
        } else {
            saveButton.setDisable(false);
        }
    }

    // ── Button Actions ────────────────────────────────────────────────
    @FXML
    private void handleAddFood() {
        if (onAddFoodAction != null) {
            onAddFoodAction.run();
        }
    }

    @FXML
    private void handleCancel() {
        if (onCancelAction != null) {
            onCancelAction.run();
        }
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();

        if (name.isEmpty()) {
            showNameMessage();
            shake(nameField);
            return;
        }

        if (draftItems.isEmpty()) {
            return;
        }

        restoreNameHelper();

        if (mode == SavedMealEditorMode.CREATE) {
            createMeal(name);
        } else {
            if (!hasChanges()) {
                return;
            }

            updateMeal(name);
        }
    }

    private void createMeal(String name) {
        List<CreateMealItemRequest> items = draftItems.stream()
                .map(item -> new CreateMealItemRequest(
                        item.foodId(),
                        item.foodName(),
                        item.brand(),
                        item.quantityGrams(),
                        item.servingSizeGrams(),
                        item.caloriesPerServing(),
                        item.proteinPerServing(),
                        item.carbsPerServing(),
                        item.fatPerServing()
                ))
                .toList();

        CreateMealRequest request = new CreateMealRequest(name, items);

        if (onCreateAction != null) {
            onCreateAction.accept(request);
        }
    }

    private void updateMeal(String name) {
        List<UpdateSavedMealItemRequest> items = draftItems.stream()
                .map(item -> new UpdateSavedMealItemRequest(
                        item.mealItemId(),
                        item.foodId(),
                        item.foodName(),
                        item.brand(),
                        item.quantityGrams(),
                        item.servingSizeGrams(),
                        item.caloriesPerServing(),
                        item.proteinPerServing(),
                        item.carbsPerServing(),
                        item.fatPerServing()
                ))
                .toList();

        UpdateSavedMealRequest request =
                new UpdateSavedMealRequest(name, items);

        if (onUpdateAction != null) {
            onUpdateAction.accept(request);
        }
    }

    @FXML
    private void handleDeleteMeal() {
        openDeleteConfirmation();
    }

    // ── Delete Confirmation ─────────────────────────────────────
    private void openDeleteConfirmation() {
        LoadedComponent<DeleteConfirmationController> confirmation = FxmlComponentLoader.load(AppConstants.Components.DELETE_CONFIRMATION);

        String mealName = nameField.getText().trim();

        confirmation.controller().setData(
                "Delete meal?",
                "Are you sure you want to delete \"" + mealName + "\"?",
                "Delete"
        );

        confirmation.controller().setOnCancelAction(
                this::closeDeleteConfirmation
        );

        confirmation.controller().setOnConfirmAction(() -> {
            closeDeleteConfirmation();

            if (onDeleteAction != null) {
                onDeleteAction.run();
            }
        });

        confirmationContainer.getChildren().setAll(
                confirmation.root()
        );

        setVisible(confirmationContainer, true);
    }

    private void closeDeleteConfirmation() {
        confirmationContainer.getChildren().clear();

        setVisible(confirmationContainer, false);
    }

    // ── Submit State ─────────────────────────────────────────────
    public void setSubmitting(boolean submitting) {
        saveButton.setDisable(submitting);
        deleteMealButton.setDisable(submitting);
    }

    // ── Name Helpers ────────────────────────────────────────────────
    private void showNameMessage() {
        setFieldMessage(nameMessage, AppConstants.Messages.INVALID_MEAL_NAME_MESSAGE, true, nameField);
    }

    private void restoreNameHelper() {
        setFieldMessage(nameMessage, AppConstants.Messages.HELPER_MEAL_NAME_MESSAGE, false, nameField);
    }

    // ── Getters ────────────────────────────────────────────────
    public Integer getMealId() {
        return mealId;
    }
}