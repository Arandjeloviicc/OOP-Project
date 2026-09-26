package com.fittrack.coordinator.nutrition.foods;


import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.components.DeleteConfirmationController;
import com.fittrack.controller.nutrition.common.editors.FoodEditorController;
import com.fittrack.dto.nutrition.food.FoodRequest;
import com.fittrack.dto.nutrition.food.FoodResponse;
import com.fittrack.ui.loader.FxmlComponentLoader;
import com.fittrack.ui.loader.LoadedComponent;
import com.fittrack.ui.overlay.OverlayManager;
import com.fittrack.ui.popup.PopupAlignment;
import com.fittrack.ui.popup.PopupOverflow;
import com.fittrack.ui.popup.PopupShellController;

import java.util.function.Consumer;

public class MyFoodsCoordinator {

    // Active Editor
    private FoodEditorController activeFoodEditor;
    private DeleteConfirmationController activeDeleteConfirmation;

    // ── Food Editor ─────────────────────────────────────────────────
    public void openCreateFoodEditor(Consumer<FoodRequest> onSave) {
        openFoodEditor(null, onSave);
    }

    public void openEditFoodEditor(FoodResponse food, Consumer<FoodRequest> onSave) {
        openFoodEditor(food, onSave);
    }

    private void openFoodEditor(FoodResponse food, Consumer<FoodRequest> onSave) {
        LoadedComponent<FoodEditorController> editor = FxmlComponentLoader.load(AppConstants.Popups.FOOD_EDITOR);

        FoodEditorController controller = editor.controller();

        activeFoodEditor = controller;

        if (food == null) {
            controller.setCreateMode();
        } else {
            controller.setEditMode(food);
        }

        controller.setOnCancelAction(
                this::closeFoodEditor
        );

        controller.setOnSaveAction(request -> {
            if (onSave != null) {
                controller.setSaving(true);
                onSave.accept(request);
            }
        });

        showFoodEditorPopup(editor);
    }

    private void showFoodEditorPopup(LoadedComponent<FoodEditorController> editor) {
        PopupShellController shell = OverlayManager.showInPopup(
                editor.root(),
                () -> activeFoodEditor = null
        );

        shell.setOverflow(PopupOverflow.SHELL_SCROLL);
        shell.setNarrowAlignment(PopupAlignment.TOP_CENTER);
        shell.setFillHeightOnNarrow(true);
    }

    public void setFoodEditorSaving(boolean saving) {
        if (activeFoodEditor != null) {
            activeFoodEditor.setSaving(saving);
        }
    }

    public void showFoodEditorSaveError(String message) {
        if (activeFoodEditor != null) {
            activeFoodEditor.showActionError(message);
        }
    }

    public void closeFoodEditor() {
        activeFoodEditor = null;
        OverlayManager.close();
    }

    // ── Delete Confirmation ─────────────────────────────────────────────────
    public void openDeleteConfirmation(String title, String message, Runnable onDelete) {
        LoadedComponent<DeleteConfirmationController> confirmation = FxmlComponentLoader.load(AppConstants.Popups.DELETE_CONFIRMATION);

        DeleteConfirmationController controller = confirmation.controller();

        activeDeleteConfirmation = controller;

        controller.setData(
                title,
                message,
                "Delete"
        );

        controller.setOnCancelAction(
                this::closeDeleteConfirmation
        );

        controller.setOnConfirmAction(() -> {
            if (onDelete != null) {
                controller.setDeleting(true);
                onDelete.run();
            }
        });

        OverlayManager.showModal(
                confirmation.root()
        );
    }

    public void setDeleteConfirmationDeleting(boolean deleting) {
        if (activeDeleteConfirmation != null) {
            activeDeleteConfirmation.setDeleting(deleting);
        }
    }

    public void showDeleteConfirmationError(String message) {
        if (activeDeleteConfirmation != null) {
            activeDeleteConfirmation.showDeleteError(message);
        }
    }

    public void closeDeleteConfirmation() {
        activeDeleteConfirmation = null;
        OverlayManager.closeModal();
    }
}