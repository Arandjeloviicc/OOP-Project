package com.fittrack.coordinator.nutrition;

import com.fittrack.config.AppConstants;
import com.fittrack.controller.nutrition.AddToMealController;
import com.fittrack.controller.nutrition.editor.MealItemEditorController;
import com.fittrack.dto.nutrition.meal.item.MealItemResponse;
import com.fittrack.model.nutrition.MealType;
import com.fittrack.ui.loader.FxmlComponentLoader;
import com.fittrack.ui.loader.LoadedComponent;
import com.fittrack.ui.overlay.OverlayManager;
import com.fittrack.ui.popup.PopupAlignment;
import com.fittrack.ui.popup.PopupOverflow;
import com.fittrack.ui.popup.PopupShellController;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DailyMealDetailsCoordinator {

    // Root
    private final StackPane rootLayout;

    // Active Meal Editor
    private MealItemEditorController activeMealItemEditor;

    // ── Constructor ──────────────────────────────────────────────────────
    public DailyMealDetailsCoordinator(StackPane rootLayout) {
        this.rootLayout = rootLayout;
    }

    // ── Add to meal ────────────────────────────────────────────
    public void openAddToMeal(MealType mealType, LocalDate mealDate, Runnable onClose, Consumer<Boolean> onBack) {
        LoadedComponent<AddToMealController> addToMeal = FxmlComponentLoader.load(AppConstants.Popups.ADD_TO_MEAL);

        AddToMealController addToMealController = addToMeal.controller();

        addToMealController.setData(mealType, mealDate);

        addToMealController.setOnCloseAction(() -> {
            if (onClose != null) {
                onClose.run();
            }
        });

        addToMealController.setOnBackAction(changed -> {
            returnToMealDetails();

            if (onBack != null) {
                onBack.accept(changed);
            }
        });

        showAddToMealPopup(addToMeal.root());
    }

    private void showAddToMealPopup(Node root) {
        PopupShellController shell = OverlayManager.replaceInPopup(root);

        shell.setOverflow(PopupOverflow.CONTENT_MANAGED);
        shell.setNarrowAlignment(PopupAlignment.TOP_CENTER);
        shell.setFillHeightOnNarrow(true);
    }

    // ── Meal item editor ───────────────────────────────────────
    public void openMealItemEditor(MealItemResponse mealItem, MealType mealType, BiConsumer<MealType, Double> onConfirm, Runnable onRemove) {
        LoadedComponent<MealItemEditorController> component = FxmlComponentLoader.load(AppConstants.Popups.MEAL_ITEM_EDITOR);

        MealItemEditorController mealItemController = component.controller();
        activeMealItemEditor = mealItemController;

        mealItemController.setData(mealItem, mealType);
        mealItemController.setCaption("Edit food");
        mealItemController.setConfirmButtonText("Save changes");

        mealItemController.setOnCancelAction(
                this::returnToMealDetails
        );

        mealItemController.setOnConfirmAction(quantityGrams -> {
            mealItemController.startConfirmLoading("Saving...");

            if (onConfirm != null) {
                onConfirm.accept(
                        mealItemController.getSelectedMealType(),
                        quantityGrams
                );
            }
        });

        mealItemController.setOnRemoveAction(() -> {
            mealItemController.setRemoveLoading(true);

            if (onRemove != null) {
                onRemove.run();
            }
        });

        showMealItemEditorPopup(component.root());
    }

    private void showMealItemEditorPopup(Node root) {
        PopupShellController shell = OverlayManager.replaceInPopup(root);

        shell.setOverflow(PopupOverflow.SHELL_SCROLL);
        shell.setNarrowAlignment(PopupAlignment.TOP_CENTER);
    }

    // ── Meal Item Editor State ──────────────────────────────────────
    public void setMealItemSaving(boolean saving) {
        if (activeMealItemEditor == null) {
            return;
        }

        if (saving) {
            activeMealItemEditor.startConfirmLoading("Saving...");
        } else {
            activeMealItemEditor.stopConfirmLoading();
        }
    }

    public void setMealItemDeleting(boolean deleting) {
        if (activeMealItemEditor != null) {
            activeMealItemEditor.setRemoveLoading(deleting);
        }
    }

    public void showMealItemError(String message) {
        if (activeMealItemEditor != null) {
            activeMealItemEditor.showActionError(message);
        }
    }

    // ── Navigation ──────────────────────────────────────────────────
    public void returnToMealDetails() {
        activeMealItemEditor = null;

        PopupShellController shell = replacePopup(rootLayout);

        shell.setOverflow(PopupOverflow.CONTENT_MANAGED);
        shell.setNarrowAlignment(PopupAlignment.TOP_CENTER);
        shell.setFillHeightOnNarrow(true);
    }

    // ── Popup Helpers ───────────────────────────────────────────────
    private PopupShellController replacePopup(Node root) {
        return OverlayManager.replaceInPopup(root);
    }
}
