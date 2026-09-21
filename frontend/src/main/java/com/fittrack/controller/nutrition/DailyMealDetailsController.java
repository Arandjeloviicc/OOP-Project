package com.fittrack.controller.nutrition;

import com.fittrack.async.AsyncTaskRunner;
import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.FormController;
import com.fittrack.controller.nutrition.components.MealItemCardController;
import com.fittrack.controller.nutrition.components.NutritionMacroPreviewController;
import com.fittrack.coordinator.nutrition.DailyMealDetailsCoordinator;
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

    // Coordinator
    private DailyMealDetailsCoordinator coordinator;

    // Actions
    private Runnable onCloseAction;

    // Service
    private final MealService mealService = new MealService();

    // ── Initialization ──────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        coordinator = new DailyMealDetailsCoordinator(rootLayout);

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
                    openMealItemEditor(mealItem)
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

        coordinator.openAddToMeal(
                mealType,
                mealDate,

                () -> {
                    if (onCloseAction != null) {
                        onCloseAction.run();
                    }
                },

                changed -> {
                    if (changed) {
                        refreshMealDetails();
                    }
                }
        );
    }

    // ── Meal Item Editor ────────────────────────────────────────────
    private void openMealItemEditor(MealItemResponse mealItem) {
        coordinator.openMealItemEditor(
                mealItem,
                mealType,

                (selectedMeal, quantityGrams) ->
                        updateMealItem(
                                mealItem,
                                selectedMeal,
                                quantityGrams
                        ),

                () -> deleteMealItem(mealItem)
        );
    }

    // ── Meal Item Actions ───────────────────────────────────────────
    private void updateMealItem(MealItemResponse mealItem, MealType selectedMeal, double quantityGrams) {
        boolean quantityChanged =
                Double.compare(
                        mealItem.quantityGrams(),
                        quantityGrams
                ) != 0;

        boolean mealChanged = !mealType.equals(selectedMeal);

        if (!quantityChanged && !mealChanged) {
            coordinator.returnToMealDetails();
            return;
        }

        UpdateMealItemRequest request = new UpdateMealItemRequest(
                quantityGrams,
                selectedMeal.getName()
        );

        AsyncTaskRunner.run(
                () -> mealService.updateMealItem(mealItem.id(), request),

                response -> {
                    dataChanged = true;

                    if (mealChanged) {
                        removeMealItem(mealItem.id());
                    } else {
                        replaceMealItem(mealItem.id(), response);
                    }

                    coordinator.returnToMealDetails();
                },

                exception -> {
                    coordinator.setMealItemSaving(false);

                    coordinator.showMealItemError("Failed to save changes. Please try again.");

                    log.error(
                            "Failed to update meal item.",
                            exception
                    );
                }
        );
    }

    private void deleteMealItem(MealItemResponse mealItem) {
          AsyncTaskRunner.run(
                () -> {
                    mealService.deleteMealItem(mealItem.id());
                    return null;
                },

                ignored -> {
                    dataChanged = true;
                    removeMealItem(mealItem.id());
                    coordinator.returnToMealDetails();
                },

                exception -> {
                    coordinator.setMealItemDeleting(false);

                    coordinator.showMealItemError("Failed to remove food. Please try again.");

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
}