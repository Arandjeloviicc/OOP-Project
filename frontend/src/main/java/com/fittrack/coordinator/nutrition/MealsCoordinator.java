package com.fittrack.coordinator.nutrition;

import com.fittrack.config.AppConstants;
import com.fittrack.controller.nutrition.AddToMealController;
import com.fittrack.controller.nutrition.DailyMealDetailsController;
import com.fittrack.controller.nutrition.dialog.MealCopyDialogController;
import com.fittrack.ui.popup.PopupAlignment;
import com.fittrack.ui.popup.PopupOverflow;
import com.fittrack.ui.popup.PopupShellController;
import com.fittrack.dto.nutrition.meal.MealResponse;
import com.fittrack.model.nutrition.MealType;
import com.fittrack.ui.loader.FxmlComponentLoader;
import com.fittrack.ui.loader.LoadedComponent;
import com.fittrack.ui.overlay.OverlayManager;
import javafx.scene.Node;

import java.time.LocalDate;
import java.util.function.BiConsumer;

public class MealsCoordinator {

    // Copy Menu
    private MealCopyDialogController activeCopyDialog;

    // ScrollPane Position Helpers
    private Runnable onOverlayOpening = () -> {};
    private Runnable onOverlayClosed = () -> {};

    // ── Overlay Lifecycle ────────────────────────────────────────────
    public void setOverlayLifecycle(Runnable onOverlayOpening, Runnable onOverlayClosed) {
        this.onOverlayOpening = onOverlayOpening;
        this.onOverlayClosed = onOverlayClosed;
    }

    // ── Add To Meal Actions ─────────────────────────────────────────────────
    public void openAddToMeal(MealType mealType, LocalDate mealDate, Runnable onChanged) {
        beforeOverlayOpen();

        LoadedComponent<AddToMealController> addToMeal = FxmlComponentLoader.load(AppConstants.Popups.ADD_TO_MEAL);

        addToMeal.controller().setData(
                mealType,
                mealDate
        );

        addToMeal.controller().setOnCloseAction(
                () -> {
                    if (onChanged != null) {
                        onChanged.run();
                    }
                }
        );

        addToMeal.controller().setOnBackAction(
                changed -> {
                    closeOverlay();

                    if (changed && onChanged != null) {
                        onChanged.run();
                    }
                }
        );

        showAddToMealPopup(addToMeal.root());
    }

    private void showAddToMealPopup(Node root) {
        PopupShellController shell = showPopup(root);

        shell.setNarrowAlignment(PopupAlignment.TOP_CENTER);
        shell.setFillHeightOnNarrow(true);
    }

    // ── Meal Details Actions ─────────────────────────────────────────────────
    public void openMealDetails(MealType mealType, LocalDate mealDate, MealResponse meal, Runnable onChanged) {
        beforeOverlayOpen();

        LoadedComponent<DailyMealDetailsController> details = FxmlComponentLoader.load(AppConstants.Popups.DAILY_MEAL_DETAILS);

        details.controller().setData(
                mealType,
                mealDate,
                meal
        );

        details.controller().setOnCloseAction(
                () -> {
                    if (onChanged != null) {
                        onChanged.run();
                    }
                }
        );

        showDailyMealDetailsPopup(details.root());
    }

    private void showDailyMealDetailsPopup(Node root) {
        PopupShellController shell = showPopup(root);

        shell.setNarrowAlignment(PopupAlignment.TOP_CENTER);
        shell.setFillHeightOnNarrow(true);
    }

    // ── ContextMenu Items Actions ─────────────────────────────────────────────────
    public void openSaveAsMeal(MealResponse meal) {
        beforeOverlayOpen();

        LoadedComponent<AddToMealController> addToMeal = FxmlComponentLoader.load(AppConstants.Popups.ADD_TO_MEAL);

        showAddToMealPopup(addToMeal.root());

        addToMeal.controller().setSaveAsMealData(meal);
    }

    public void openCopyFrom(MealType currentMealType, LocalDate currentDate, BiConsumer<MealType, LocalDate> onAvailabilityCheck, BiConsumer<MealType, LocalDate> onCopy, Runnable onClose) {
        beforeOverlayOpen();

        LoadedComponent<MealCopyDialogController> copyDialog = FxmlComponentLoader.load(AppConstants.Popups.MEAL_COPY_DIALOG);

        MealCopyDialogController controller = copyDialog.controller();

        activeCopyDialog = controller;

        controller.setOnCloseAction(() -> {
            activeCopyDialog = null;

            if (onClose != null) {
                onClose.run();
            }

            closeOverlay();
        });

        controller.setOnAvailabilityCheckAction(
                (sourceMealType, sourceDate) -> {
                    controller.setCheckingAvailability();

                    if (onAvailabilityCheck != null) {
                        onAvailabilityCheck.accept(
                                sourceMealType,
                                sourceDate
                        );
                    }
                }
        );

        controller.setOnCopyAction(
                (sourceMealType, sourceDate) -> {
                    if (onCopy != null) {
                        controller.setCopying(true);

                        onCopy.accept(
                                sourceMealType,
                                sourceDate
                        );
                    }
                }
        );

        controller.setCopyFrom(
                currentMealType,
                currentDate
        );

        showOverlay(copyDialog.root());
    }

    public void openCopyTo(MealType currentMealType, LocalDate currentDate, BiConsumer<MealType, LocalDate> onCopy) {
        beforeOverlayOpen();

        LoadedComponent<MealCopyDialogController> copyDialog = FxmlComponentLoader.load(AppConstants.Popups.MEAL_COPY_DIALOG);

        MealCopyDialogController controller = copyDialog.controller();

        activeCopyDialog = controller;

        controller.setOnCloseAction(() -> {
            activeCopyDialog = null;
            closeOverlay();
        });

        controller.setOnCopyAction(
                (targetMealType, targetDate) -> {
                    if (onCopy != null) {
                        controller.setCopying(true);

                        onCopy.accept(
                                targetMealType,
                                targetDate
                        );
                    }
                }
        );

        controller.setCopyTo(
                currentMealType,
                currentDate
        );

        showOverlay(copyDialog.root());
    }

    public void setMealAvailability(boolean available) {
        if (activeCopyDialog == null) {
            return;
        }

        if (available) {
            activeCopyDialog.setMealAvailable();
        } else {
            activeCopyDialog.setMealUnavailable();
        }
    }

    public void setMealAvailabilityCheckFailed() {
        if (activeCopyDialog != null) {
            activeCopyDialog.setAvailabilityCheckFailed();
        }
    }

    public void closeCopyDialog() {
        activeCopyDialog = null;
        closeOverlay();
    }

    // ── Overlay Helpers ──────────────────────────────────────────────
    private void beforeOverlayOpen() {
        onOverlayOpening.run();
    }

    private void showOverlay(Node root) {
        OverlayManager.show(root, onOverlayClosed);
    }

    private PopupShellController showPopup(Node root) {
        PopupShellController shell = OverlayManager.showInPopup(root, onOverlayClosed);

        shell.setOverflow(PopupOverflow.CONTENT_MANAGED);

        return shell;
    }

    private void closeOverlay() {
        OverlayManager.close();
    }

    // ── Copy Dialog State ─────────────────────────────────────────
    public void setCopying(boolean copying) {
        if (activeCopyDialog != null) {
            activeCopyDialog.setCopying(copying);
        }
    }

    public void showCopyError(String message) {
        if (activeCopyDialog != null) {
            activeCopyDialog.showActionError(message);
        }
    }
}