package com.fittrack.controller.nutrition;

import com.fittrack.async.AsyncTaskRunner;
import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.FormController;
import com.fittrack.controller.nutrition.components.MealItemCardController;
import com.fittrack.controller.nutrition.editor.MealItemEditorController;
import com.fittrack.controller.nutrition.components.NutritionMacroPreviewController;
import com.fittrack.controller.popup.PopupAlignment;
import com.fittrack.controller.popup.PopupOverflow;
import com.fittrack.controller.popup.PopupShellController;
import com.fittrack.dto.nutrition.meal.item.MealItemResponse;
import com.fittrack.dto.nutrition.meal.MealResponse;
import com.fittrack.dto.nutrition.meal.item.UpdateMealItemRequest;
import com.fittrack.model.nutrition.DailyNutritionTotals;
import com.fittrack.model.nutrition.MealType;
import com.fittrack.service.nutrition.MealService;
import com.fittrack.service.nutrition.NutritionCalculationService;
import com.fittrack.ui.loader.FxmlComponentLoader;
import com.fittrack.ui.loader.LoadedComponent;
import com.fittrack.ui.overlay.OverlayManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DailyMealDetailsController extends FormController implements Initializable {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(DailyMealDetailsController.class);

    // Meal Details
    @FXML private StackPane rootLayout;
    @FXML private Label titleLabel;
    @FXML private VBox macroPreviewContainer;
    @FXML private Label itemCountLabel;
    @FXML private VBox itemsContainer;

    // Attributes
    private MealType mealType;
    private LocalDate mealDate;
    private MealResponse currentMeal;

    private boolean dataChanged;

    // Macro Preview
    private NutritionMacroPreviewController macroPreview;

    // Actions
    private Runnable onCloseAction;

    // Service
    private final MealService mealService = new MealService();

    // ── Initialization ──────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize Macro Preview
        initializeMacroPreview();
    }

    private void initializeMacroPreview() {
        LoadedComponent<NutritionMacroPreviewController> preview = FxmlComponentLoader.load(AppConstants.Components.NUTRITION_MACRO_PREVIEW);

        macroPreview = preview.controller();

        macroPreviewContainer.getChildren().setAll(preview.root());
    }

    // ── Configuration ───────────────────────────────────────────────
    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    // ── Set Data ─────────────────────────────────────────────────
    public void setData(MealType mealType, LocalDate mealDate, MealResponse meal) {
        this.mealType = mealType;
        this.mealDate = mealDate;
        this.currentMeal = meal;
        this.dataChanged = false;

        renderMeal();
    }

    // ── Rendering ───────────────────────────────────────────────────
    private void renderMeal() {
        // Title
        titleLabel.setText(mealType.getName());

        itemsContainer.getChildren().clear();

        if (currentMeal == null || currentMeal.items().isEmpty()) {
            itemCountLabel.setText("0 items");

            macroPreview.setData(
                    0,
                    0,
                    0,
                    0
            );

            return;
        }

        // Macro Preview
        DailyNutritionTotals totals = NutritionCalculationService.calculateMealNutritionTotals(currentMeal);
        macroPreview.setData(
                totals.calories(),
                totals.carbs(),
                totals.fat(),
                totals.protein()
        );

        itemCountLabel.setText(
                currentMeal.items().size() == 1
                        ? "1 item"
                        : currentMeal.items().size() + " items"
        );

        for (MealItemResponse mealItem : currentMeal.items()) {
            LoadedComponent<MealItemCardController> card = FxmlComponentLoader.load(AppConstants.Components.MEAL_ITEM_CARD);

            double calories = NutritionCalculationService.calculateFoodCalories(mealItem);

            card.controller().setData(
                    mealItem.foodName(),
                    mealItem.quantityGrams(),
                    calories
            );

            card.controller().setOnOpenAction(() ->
                    openFoodDetails(mealItem)
            );

            itemsContainer.getChildren().add(card.root());
        }
    }

    // ── Button Actions ─────────────────────────────────────────────────
    @FXML
    private void handleClose() {
        OverlayManager.close();

        if (dataChanged && onCloseAction != null) {
            onCloseAction.run();
        }
    }

    @FXML
    private void handleLogMore() {
        if (dataChanged && onCloseAction != null) {
            onCloseAction.run();
            dataChanged = false;
        }

        LoadedComponent<AddToMealController> addFood = FxmlComponentLoader.load(AppConstants.Views.ADD_TO_MEAL);

        AddToMealController controller = addFood.controller();

        controller.setData(mealType, mealDate);

        controller.setOnCloseAction(() -> {
            if (onCloseAction != null) {
                onCloseAction.run();
            }
        });

        controller.setOnBackAction(changed -> {
            showMealDetailsPopup();

            if (changed) {
                refreshMealDetails();
            }
        });

        showAddToMealPopup(addFood.root());
    }

    // ── Meal Item Editor ────────────────────────────────────────────
    private void openFoodDetails(MealItemResponse mealItem) {
        LoadedComponent<MealItemEditorController> details = FxmlComponentLoader.load(AppConstants.Components.MEAL_ITEM_EDITOR);

        details.controller().setData(mealItem, mealType);
        details.controller().setCaption("Edit food");
        details.controller().setConfirmButtonText("Save changes");

        details.controller().setOnCancelAction(
                this::closeFoodDetails
        );

        details.controller().setOnConfirmAction(
                quantityGrams -> updateMealItem(
                        mealItem,
                        details.controller().getSelectedMealType(),
                        quantityGrams,
                        details.controller()
                )
        );

        details.controller().setOnRemoveAction(
                () -> deleteMealItem(
                        mealItem,
                        details.controller()
                )
        );

        showMealItemEditorPopup(details.root());
    }

    private void closeFoodDetails() {
        showMealDetailsPopup();
    }

    // ── Meal Item Actions ───────────────────────────────────────────
    private void updateMealItem(MealItemResponse mealItem, MealType selectedMeal, double quantityGrams,  MealItemEditorController editor) {
        boolean quantityChanged =
                Double.compare(
                        mealItem.quantityGrams(),
                        quantityGrams
                ) != 0;

        boolean mealChanged = !mealType.equals(selectedMeal);

        if (!quantityChanged && !mealChanged) {
            closeFoodDetails();
            return;
        }

        UpdateMealItemRequest request = new UpdateMealItemRequest(
                quantityGrams,
                selectedMeal.getName()
        );

        editor.startConfirmLoading("Saving...");

        AsyncTaskRunner.run(
                () -> mealService.updateMealItem(mealItem.id(), request),

                response -> {
                    dataChanged = true;

                    if (mealChanged) {
                        removeMealItem(mealItem.id());
                    } else {
                        replaceMealItem(mealItem.id(), response);
                    }

                    closeFoodDetails();
                },

                exception -> {
                    editor.stopConfirmLoading();

                    log.error(
                            "Failed to update meal item.",
                            exception
                    );
                }
        );
    }

    private void deleteMealItem(MealItemResponse mealItem, MealItemEditorController editor) {
        editor.setRemoveLoading(true);

        AsyncTaskRunner.run(
                () -> {
                    mealService.deleteMealItem(mealItem.id());
                    return null;
                },

                ignored -> {
                    dataChanged = true;
                    removeMealItem(mealItem.id());
                    closeFoodDetails();
                },

                exception -> {
                    editor.setRemoveLoading(false);

                    log.error(
                            "Failed to delete meal item.",
                            exception
                    );
                }
        );
    }

    // ── Meal Item State ─────────────────────────────────────────────
    private void replaceMealItem(Integer oldItemId, MealItemResponse updatedItem) {
        if (currentMeal == null) {
            return;
        }

        List<MealItemResponse> updatedItems = new ArrayList<>(currentMeal.items());

        for (int i = 0; i < updatedItems.size(); i++) {
            if (updatedItems.get(i).id().equals(oldItemId)) {
                updatedItems.set(i, updatedItem);
                break;
            }
        }

        currentMeal = new MealResponse(
                currentMeal.id(),
                currentMeal.name(),
                currentMeal.mealDate(),
                updatedItems
        );

        renderMeal();
    }

    private void removeMealItem(Integer mealItemId) {
        if (currentMeal == null) {
            return;
        }

        List<MealItemResponse> updatedItems =
                currentMeal.items()
                        .stream()
                        .filter(item ->
                                !item.id().equals(mealItemId)
                        )
                        .toList();

        currentMeal = new MealResponse(
                currentMeal.id(),
                currentMeal.name(),
                currentMeal.mealDate(),
                updatedItems
        );

        renderMeal();
    }

    // ── Refresh ─────────────────────────────────────────────────────
    private void refreshMealDetails() {
        AsyncTaskRunner.run(
                () -> mealService.getMealsForDate(mealDate),

                meals -> {
                    currentMeal = meals.stream()
                            .filter(item -> item.name().equals(mealType.getName()))
                            .findFirst()
                            .orElse(null);

                    renderMeal();

                    dataChanged = true;
                },

                exception -> log.error(
                        "Failed to refresh meal details.",
                        exception
                )
        );
    }

    // ── Popup Navigation ────────────────────────────────────────────
    private void showMealDetailsPopup() {
        PopupShellController shell = OverlayManager.replaceInPopup(rootLayout);

        shell.setOverflow(PopupOverflow.CONTENT_MANAGED);
        shell.setNarrowAlignment(PopupAlignment.TOP_CENTER);
        shell.setFillHeightOnNarrow(true);
    }

    private void showAddToMealPopup(Node root) {
        PopupShellController shell = OverlayManager.replaceInPopup(root);

        shell.setOverflow(PopupOverflow.CONTENT_MANAGED);
        shell.setNarrowAlignment(PopupAlignment.TOP_CENTER);
        shell.setFillHeightOnNarrow(true);
    }

    private void showMealItemEditorPopup(Node root) {
        PopupShellController shell =
                OverlayManager.replaceInPopup(root);

        shell.setOverflow(PopupOverflow.SHELL_SCROLL);
        shell.setNarrowAlignment(PopupAlignment.TOP_CENTER);
    }
}