package com.fittrack.coordinator.nutrition;

import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.components.DeleteConfirmationController;
import com.fittrack.controller.nutrition.editor.FoodEditorController;
import com.fittrack.controller.nutrition.editor.MealItemEditorController;
import com.fittrack.controller.nutrition.editor.SavedMealEditorController;
import com.fittrack.ui.popup.PopupAlignment;
import com.fittrack.ui.popup.PopupOverflow;
import com.fittrack.ui.popup.PopupShellController;
import com.fittrack.dto.nutrition.food.CreateFoodRequest;
import com.fittrack.dto.nutrition.food.FoodResponse;
import com.fittrack.dto.nutrition.meal.CreateMealRequest;
import com.fittrack.dto.nutrition.meal.MealResponse;
import com.fittrack.dto.nutrition.meal.UpdateSavedMealRequest;
import com.fittrack.dto.nutrition.meal.item.MealItemDraft;
import com.fittrack.model.nutrition.MealType;
import com.fittrack.ui.loader.FxmlComponentLoader;
import com.fittrack.ui.loader.LoadedComponent;
import com.fittrack.ui.overlay.OverlayManager;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AddToMealCoordinator {

    // Selection View
    private final StackPane selectionRoot;

    // Active Editor View
    private Node activeEditorRoot;

    // Active Editors
    private SavedMealEditorController activeSavedMealEditor;
    private FoodEditorController activeFoodEditor;
    private MealItemEditorController activeMealItemEditor;

    // ── Constructor ──────────────────────────────────────────────────────
    public AddToMealCoordinator(StackPane selectionRoot) {
        this.selectionRoot = selectionRoot;
    }

    // ── State ──────────────────────────────────────────────────────
    public boolean hasActiveSavedMealEditor() {
        return activeSavedMealEditor != null;
    }

    public void openMealItemEditor(FoodResponse food, MealType mealType, Consumer<Double> onAddToDraft, BiConsumer<MealType, Double> onAddToMeal) {
        LoadedComponent<MealItemEditorController> details = FxmlComponentLoader.load(AppConstants.Popups.MEAL_ITEM_EDITOR);

        MealItemEditorController detailsController = details.controller();
        activeMealItemEditor = detailsController;

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
                            detailsController.startConfirmLoading("Adding...");

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

        showMealItemEditorPopup(details.root());
    }

    private void openDraftItemEditor(MealItemDraft item) {
        if (activeSavedMealEditor == null) {
            throw new IllegalStateException("No active saved meal editor.");
        }

        LoadedComponent<MealItemEditorController> editor = FxmlComponentLoader.load(AppConstants.Popups.MEAL_ITEM_EDITOR);

        MealItemEditorController editorController =
                editor.controller();

        activeMealItemEditor = editorController;

        editorController.setData(item);
        editorController.setCaption("Edit food");
        editorController.setConfirmButtonText("Save changes");

        editorController.setOnCancelAction(
                this::returnToMealEditor
        );

        editorController.setOnConfirmAction(quantityGrams -> {
            activeSavedMealEditor.updateDraftItem(item, quantityGrams);

            returnToMealEditor();
        });

        editorController.setOnRemoveAction(() -> {
            activeSavedMealEditor.removeDraftItem(item);

            returnToMealEditor();
        });

        showMealItemEditorPopup(editor.root());
    }

    public void closeFoodDetails() {
        activeMealItemEditor = null;
        showSelectionPopup();
    }

    // ── Saved Meal Editor ──────────────────────────────────────────
    public void openSavedMealEditor(MealResponse meal, Runnable onCancel, Runnable onAddFood, Consumer<UpdateSavedMealRequest> onUpdate, Runnable onDelete) {
        LoadedComponent<SavedMealEditorController> editor = FxmlComponentLoader.load(AppConstants.Popups.SAVED_MEAL_EDITOR);

        SavedMealEditorController editorController = editor.controller();
        activeSavedMealEditor = editorController;

        activeSavedMealEditor.setEditMode(meal);

        activeSavedMealEditor.setOnCancelAction(() -> {
            closeEditor();

            if (onCancel != null) {
                onCancel.run();
            }
        });

        editorController.setOnAddFoodAction(onAddFood);

        editorController.setOnEditItemAction(
                this::openDraftItemEditor
        );

        editorController.setOnUpdateAction(request -> {
            editorController.setSaving(true);

            if (onUpdate != null) {
                onUpdate.accept(request);
            }
        });

        editorController.setOnDeleteAction(
                () -> openDeleteConfirmation(
                        editorController,
                        onDelete
                )
        );

        activeEditorRoot = editor.root();
        showSavedMealEditorPopup(editor.root());
    }

    public void openSaveAsMealEditor(MealResponse sourceMeal, Runnable onCancel, Runnable onAddFood, Consumer<CreateMealRequest> onCreate) {
        LoadedComponent<SavedMealEditorController> editor = FxmlComponentLoader.load(AppConstants.Popups.SAVED_MEAL_EDITOR);

        SavedMealEditorController editorController = editor.controller();
        activeSavedMealEditor = editorController;

        editorController.setCreateMode(sourceMeal);

        editorController.setOnEditItemAction(
                this::openDraftItemEditor
        );

        editorController.setOnCancelAction(() -> {
            closeEditor();

            if (onCancel != null) {
                onCancel.run();
            }
        });

        editorController.setOnAddFoodAction(onAddFood);

        editorController.setOnCreateAction(request -> {
            editorController.setSaving(true);

            if (onCreate != null) {
                onCreate.accept(request);
            }
        });

        activeEditorRoot = editor.root();
        showSavedMealEditorPopup(editor.root());
    }

    public void openCreateMeal(Runnable onCancel, Runnable onAddFood, Consumer<CreateMealRequest> onCreate) {
        LoadedComponent<SavedMealEditorController> editor = FxmlComponentLoader.load(AppConstants.Popups.SAVED_MEAL_EDITOR);

        SavedMealEditorController editorController = editor.controller();
        activeSavedMealEditor = editorController;

        editorController.setCreateMode();

        editorController.setOnEditItemAction(
                this::openDraftItemEditor
        );

        editorController.setOnCancelAction(() -> {
            closeEditor();

            if (onCancel != null) {
                onCancel.run();
            }
        });

        editorController.setOnAddFoodAction(onAddFood);

        editorController.setOnCreateAction(request -> {
            editorController.setSaving(true);

            if (onCreate != null) {
                onCreate.accept(request);
            }
        });

        activeEditorRoot = editor.root();
        showSavedMealEditorPopup(editor.root());
    }

    private void showSavedMealEditorPopup(Node root) {
        PopupShellController shell = OverlayManager.replaceInPopup(root);

        shell.setOverflow(PopupOverflow.CONTENT_MANAGED);
        shell.setNarrowAlignment(PopupAlignment.TOP_CENTER);
        shell.setFillHeightOnNarrow(true);
    }

    // ── Food Editor ────────────────────────────────────────────────
    public void openCreateFood(Consumer<CreateFoodRequest> onCreate) {
        LoadedComponent<FoodEditorController> editor = FxmlComponentLoader.load(AppConstants.Popups.FOOD_EDITOR);

        FoodEditorController editorController = editor.controller();
        activeFoodEditor = editorController;
        activeEditorRoot = editor.root();

        editorController.setCreateMode();

        editorController.setOnCancelAction(
                this::closeEditor
        );

        editorController.setOnCreateAction(request -> {
            editorController.setSubmitting(true);

            if (onCreate != null) {
                onCreate.accept(request);
            }
        });

        showFoodEditorPopup(editor.root());
    }

    private void showFoodEditorPopup(Node root) {
        PopupShellController shell = OverlayManager.replaceInPopup(root);

        shell.setOverflow(PopupOverflow.SHELL_SCROLL);
        shell.setNarrowAlignment(PopupAlignment.TOP_CENTER);
        shell.setFillHeightOnNarrow(true);
    }

    // ── Delete Confirmation ─────────────────────────────────────────────────
    private void openDeleteConfirmation(SavedMealEditorController editor, Runnable onDelete) {
        LoadedComponent<DeleteConfirmationController> confirmation = FxmlComponentLoader.load(AppConstants.Popups.DELETE_CONFIRMATION);

        DeleteConfirmationController controller = confirmation.controller();

        controller.setData(
                "Delete meal?",
                "Are you sure you want to delete \""
                        + editor.getMealName()
                        + "\"?",
                "Delete"
        );

        controller.setOnCancelAction(
                OverlayManager::closeModal
        );

        controller.setOnConfirmAction(() -> {
            OverlayManager.closeModal();

            if (onDelete != null) {
                editor.setDeleting(true);
                onDelete.run();
            }
        });

        OverlayManager.showModal(
                confirmation.root()
        );
    }

    // ── Navigation ─────────────────────────────────────────────────
    public void showSelection() {
        showSelectionPopup();
    }

    public void closeEditor() {
        activeSavedMealEditor = null;
        activeFoodEditor = null;
        activeMealItemEditor = null;
        activeEditorRoot = null;

        showSelectionPopup();
    }

    public void returnToMealEditor() {
        activeMealItemEditor = null;

        if (activeEditorRoot == null) {
            throw new IllegalStateException("No active meal editor.");
        }

        showSavedMealEditorPopup(activeEditorRoot);
    }

    private void showSelectionPopup() {
        PopupShellController shell = OverlayManager.replaceInPopup(selectionRoot);

        shell.setOverflow(PopupOverflow.CONTENT_MANAGED);
        shell.setNarrowAlignment(PopupAlignment.TOP_CENTER);
        shell.setFillHeightOnNarrow(true);
    }

    private void showMealItemEditorPopup(Node root) {
        PopupShellController shell = OverlayManager.replaceInPopup(root);

        shell.setOverflow(PopupOverflow.SHELL_SCROLL);
        shell.setNarrowAlignment(PopupAlignment.TOP_CENTER);
    }

    // ── Helpers ────────────────────────────────────────────────────
    public boolean isEditingSavedMeal(Integer mealId) {
        return activeSavedMealEditor != null
                && activeSavedMealEditor.getMealId() != null
                && activeSavedMealEditor.getMealId().equals(mealId);
    }

    public void addDraftItem(MealItemDraft draftItem) {
        if (activeSavedMealEditor == null) {
            throw new IllegalStateException("No active saved meal editor.");
        }

        activeSavedMealEditor.addDraftItem(draftItem);
    }

    public void addDraftItems(List<MealItemDraft> draftItems) {
        if (activeSavedMealEditor == null) {
            throw new IllegalStateException("No active saved meal editor.");
        }

        activeSavedMealEditor.addDraftItems(draftItems);
    }

    public void setSavedMealEditorSaving(boolean saving) {
        if (activeSavedMealEditor != null) {
            activeSavedMealEditor.setSaving(saving);
        }
    }

    public void setSavedMealEditorDeleting(boolean deleting) {
        if (activeSavedMealEditor != null) {
            activeSavedMealEditor.setDeleting(deleting);
        }
    }

    public void showSavedMealEditorError(String message) {
        if (activeSavedMealEditor != null) {
            activeSavedMealEditor.showActionError(message);
        }
    }

    public void setFoodEditorSubmitting(boolean submitting) {
        if (activeFoodEditor != null) {
            activeFoodEditor.setSubmitting(submitting);
        }
    }

    public void showFoodEditorError(String message) {
        if (activeFoodEditor != null) {
            activeFoodEditor.showActionError(message);
        }
    }

    public void resetMealItemAdding() {
        if (activeMealItemEditor != null) {
            activeMealItemEditor.stopConfirmLoading();
        }
    }
}