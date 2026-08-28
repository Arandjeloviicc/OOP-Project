package com.fittrack.controller.nutrition.coordinator;

import com.fittrack.config.AppConstants;
import com.fittrack.controller.nutrition.components.FoodEditorController;
import com.fittrack.controller.nutrition.components.MealItemEditorController;
import com.fittrack.controller.nutrition.components.SavedMealEditorController;
import com.fittrack.dto.nutrition.food.CreateFoodRequest;
import com.fittrack.dto.nutrition.food.FoodResponse;
import com.fittrack.dto.nutrition.meal.CreateMealRequest;
import com.fittrack.dto.nutrition.meal.MealResponse;
import com.fittrack.dto.nutrition.meal.UpdateSavedMealRequest;
import com.fittrack.model.nutrition.MealType;
import com.fittrack.ui.FxmlComponentLoader;
import com.fittrack.ui.LoadedComponent;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AddToMealCoordinator {

    // Containers
    private final VBox selectionContainer;
    private final StackPane itemDetailsContainer;
    private final StackPane editorContainer;

    // Active Meal Editor
    private SavedMealEditorController activeSavedMealEditor;

    // ── Constructor ──────────────────────────────────────────────────────
    public AddToMealCoordinator(VBox selectionContainer, StackPane itemDetailsContainer, StackPane editorContainer) {
        this.selectionContainer = selectionContainer;
        this.itemDetailsContainer = itemDetailsContainer;
        this.editorContainer = editorContainer;
    }

    // ── State ──────────────────────────────────────────────────────
    public boolean hasActiveSavedMealEditor() {
        return activeSavedMealEditor != null;
    }

    public SavedMealEditorController getActiveSavedMealEditor() {
        return activeSavedMealEditor;
    }

    public void openFoodDetails(FoodResponse food, MealType mealType, Consumer<Double> onAddToDraft, BiConsumer<MealType, Double> onAddToMeal) {
        LoadedComponent<MealItemEditorController> details = FxmlComponentLoader.load(AppConstants.Components.MEAL_ITEM_EDITOR);

        MealItemEditorController detailsController = details.controller();

        if (activeSavedMealEditor != null) {
            detailsController.setDraftData(food);
            detailsController.setCaption("Add food");
            detailsController.setConfirmButtonText("Add food");

            detailsController.setOnConfirmAction(
                    quantityGrams -> {
                        if (onAddToDraft != null) {
                            onAddToDraft.accept(quantityGrams);
                        }
                    }
            );
        } else {
            detailsController.setData(food, mealType);
            detailsController.setCaption("Add food");
            detailsController.setConfirmButtonText("Add to meal");

            detailsController.setOnConfirmAction(
                    quantityGrams -> {
                        if (onAddToMeal != null) {
                            onAddToMeal.accept(
                                    detailsController.getSelectedMealType(),
                                    quantityGrams
                            );
                        }
                    }
            );
        }

        detailsController.setOnCancelAction(
                this::closeFoodDetails
        );

        itemDetailsContainer.getChildren().setAll(details.root());

        setVisible(selectionContainer, false);
        setVisible(editorContainer, false);
        setVisible(itemDetailsContainer, true);
    }

    public void closeFoodDetails() {
        itemDetailsContainer.getChildren().clear();

        setVisible(itemDetailsContainer, false);
        setVisible(selectionContainer, true);
    }

    // ── Saved Meal Editor ──────────────────────────────────────────
    public void openSavedMealEditor(MealResponse meal, Runnable onCancel, Runnable onAddFood, Consumer<UpdateSavedMealRequest> onUpdate, Runnable onDelete) {
        LoadedComponent<SavedMealEditorController> editor = FxmlComponentLoader.load(AppConstants.Components.SAVED_MEAL_EDITOR);

        activeSavedMealEditor = editor.controller();
        activeSavedMealEditor.setEditMode(meal);

        activeSavedMealEditor.setOnCancelAction(() -> {
            closeEditor();

            if (onCancel != null) {
                onCancel.run();
            }
        });

        activeSavedMealEditor.setOnAddFoodAction(onAddFood);

        activeSavedMealEditor.setOnUpdateAction(onUpdate);

        activeSavedMealEditor.setOnDeleteAction(onDelete);

        showEditor(editor.root());
    }

    public void openSaveAsMealEditor(MealResponse sourceMeal, Runnable onCancel, Runnable onAddFood, Consumer<CreateMealRequest> onCreate) {
        LoadedComponent<SavedMealEditorController> editor = FxmlComponentLoader.load(AppConstants.Components.SAVED_MEAL_EDITOR);

        activeSavedMealEditor = editor.controller();
        activeSavedMealEditor.setCreateMode(sourceMeal);

        activeSavedMealEditor.setOnCancelAction(() -> {
            closeEditor();

            if (onCancel != null) {
                onCancel.run();
            }
        });

        activeSavedMealEditor.setOnAddFoodAction(onAddFood);

        activeSavedMealEditor.setOnCreateAction(onCreate);

        showEditor(editor.root());
    }

    public void openCreateMeal(Runnable onCancel, Runnable onAddFood, Consumer<CreateMealRequest> onCreate) {
        LoadedComponent<SavedMealEditorController> editor = FxmlComponentLoader.load(AppConstants.Components.SAVED_MEAL_EDITOR);

        activeSavedMealEditor = editor.controller();
        activeSavedMealEditor.setCreateMode();

        activeSavedMealEditor.setOnCancelAction(() -> {
            closeEditor();

            if (onCancel != null) {
                onCancel.run();
            }
        });

        activeSavedMealEditor.setOnAddFoodAction(onAddFood);

        activeSavedMealEditor.setOnCreateAction(onCreate);

        showEditor(editor.root());
    }

    public void returnToMealEditor() {
        itemDetailsContainer.getChildren().clear();

        setVisible(itemDetailsContainer, false);
        setVisible(selectionContainer, false);
        setVisible(editorContainer, true);
    }

    // ── Food Editor ────────────────────────────────────────────────
    public void openCreateFood(BiConsumer<CreateFoodRequest, FoodEditorController> onCreate) {
        LoadedComponent<FoodEditorController> editor = FxmlComponentLoader.load(AppConstants.Components.FOOD_EDITOR);

        FoodEditorController editorController = editor.controller();

        editorController.setCreateMode();

        editorController.setOnCancelAction(
                this::closeEditor
        );

        editorController.setOnCreateAction(
                request -> {
                    if (onCreate != null) {
                        onCreate.accept(
                                request,
                                editorController
                        );
                    }
                }
        );

        showEditor(editor.root());
    }

    // ── Navigation ─────────────────────────────────────────────────
    public void showSelection() {
        setVisible(editorContainer, false);
        setVisible(itemDetailsContainer, false);
        setVisible(selectionContainer, true);
    }

    private void showEditor(Node editorRoot) {
        editorContainer.getChildren().setAll(editorRoot);

        setVisible(selectionContainer, false);
        setVisible(itemDetailsContainer, false);
        setVisible(editorContainer, true);
    }

    public void closeEditor() {
        editorContainer.getChildren().clear();

        activeSavedMealEditor = null;

        setVisible(editorContainer, false);
        setVisible(selectionContainer, true);
    }

    // ── Helpers ────────────────────────────────────────────────────
    private void setVisible(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }
}