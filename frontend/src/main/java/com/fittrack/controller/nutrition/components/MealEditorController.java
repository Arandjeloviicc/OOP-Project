package com.fittrack.controller.nutrition.components;

import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.FormController;
import com.fittrack.dto.nutrition.meal.CreateMealRequest;
import com.fittrack.dto.nutrition.meal.item.CreateMealItemRequest;
import com.fittrack.dto.nutrition.meal.item.MealItemDraft;
import com.fittrack.model.nutrition.MealEditorMode;
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

public class MealEditorController extends FormController implements Initializable {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(MealEditorController.class);

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

    // Mode (Create/Edit)
    private MealEditorMode mode = MealEditorMode.CREATE;

    // Macro Preview
    private NutritionMacroPreviewController macroPreview;

    // Added items
    private final List<MealItemDraft> draftItems = new ArrayList<>();

    // Actions
    private Runnable onCancelAction;
    private Runnable onAddFoodAction;
    private Consumer<CreateMealRequest> onSaveAction;

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
        nameField.textProperty().addListener((observable, oldValue, newValue) -> restoreNameHelper());
        restoreNameHelper();
    }

    // ── Configuration ──────────────────────────────────────────
    public void setOnCancelAction(Runnable onCancelAction) {
        this.onCancelAction = onCancelAction;
    }

    public void setOnAddFoodAction(Runnable onAddFoodAction) {
        this.onAddFoodAction = onAddFoodAction;
    }

    public void setOnSaveAction(Consumer<CreateMealRequest> onSaveAction) {
        this.onSaveAction = onSaveAction;
    }

    public void setCreateMode() {
        mode = MealEditorMode.CREATE;

        titleLabel.setText("Create Meal");
        saveButton.setText("Create meal");

        setVisible(deleteMealButton, false);

        nameField.clear();
        draftItems.clear();

        refreshDraft();
    }

    public void setEditMode() {
        mode = MealEditorMode.EDIT;

        titleLabel.setText("Edit Meal");
        saveButton.setText("Save changes");

        setVisible(deleteMealButton, true);

        nameField.clear();
        draftItems.clear();

        refreshDraft();
    }

    // ── Draft State ────────────────────────────────────────────
    public void addDraftItem(MealItemDraft item) {
        draftItems.add(item);
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
        LoadedComponent<MealItemDetailsController> details = FxmlComponentLoader.load(AppConstants.Components.MEAL_ITEM_DETAILS);

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
        saveButton.setVisible(!draftItems.isEmpty());
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

        List<CreateMealItemRequest> items = draftItems.stream()
                        .map(item -> new CreateMealItemRequest(
                                item.foodId(),
                                item.quantityGrams()
                        )).toList();

        CreateMealRequest request = new CreateMealRequest(name, items);

        if (onSaveAction != null) {
            onSaveAction.accept(request);
        }
    }

    @FXML
    private void handleDeleteMeal() {

    }

    // ── Name Helpers ────────────────────────────────────────────────
    private void showNameMessage() {
        setFieldMessage(nameMessage, AppConstants.Messages.INVALID_MEAL_NAME_MESSAGE, true, nameField);
    }

    private void restoreNameHelper() {
        setFieldMessage(nameMessage, AppConstants.Messages.HELPER_MEAL_NAME_MESSAGE, false, nameField);
    }
}
