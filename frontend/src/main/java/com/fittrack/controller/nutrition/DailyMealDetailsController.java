package com.fittrack.controller.nutrition;

import com.fittrack.async.AsyncTaskRunner;
import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.FormController;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.controller.nutrition.components.MealItemCardController;
import com.fittrack.controller.nutrition.components.MealItemEditorController;
import com.fittrack.controller.nutrition.components.NutritionMacroPreviewController;
import com.fittrack.dto.nutrition.meal.item.MealItemResponse;
import com.fittrack.dto.nutrition.meal.MealResponse;
import com.fittrack.dto.nutrition.meal.item.UpdateMealItemRequest;
import com.fittrack.model.nutrition.DailyNutritionTotals;
import com.fittrack.model.nutrition.MealType;
import com.fittrack.service.nutrition.MealService;
import com.fittrack.service.nutrition.NutritionCalculationService;
import com.fittrack.ui.FxmlComponentLoader;
import com.fittrack.ui.LoadedComponent;
import com.fittrack.ui.OverlayManager;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DailyMealDetailsController extends FormController implements Initializable, ResponsiveLayout {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(DailyMealDetailsController.class);

    // Meal Details
    @FXML private StackPane rootLayout;
    @FXML private VBox dialogContainer;
    @FXML private Label titleLabel;
    @FXML private VBox macroPreviewContainer;
    @FXML private Label itemCountLabel;
    @FXML private VBox itemsContainer;

    // Update Food
    @FXML private VBox foodDetailsContainer;

    // Macro Preview
    private NutritionMacroPreviewController macroPreview;

    // Attributes
    private MealType mealType;
    private LocalDate mealDate;

    // Current Meal
    private MealResponse currentMeal;

    // Action Helpers
    private Runnable onCloseAction;
    private boolean dataChanged;

    // Responsive Helpers
    private static final int NARROW_BREAKPOINT = 650;
    private static final int SHORT_BREAKPOINT = 650;
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");
    private static final PseudoClass SHORT = PseudoClass.getPseudoClass("short");

    // Service
    private final MealService mealService = new MealService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize Macro Preview
        initializeMacroPreview();

        // Responsive Initialize
        initializeResponsiveWidthLayout(rootLayout, NARROW_BREAKPOINT);
        initializeResponsiveHeightLayout(rootLayout, SHORT_BREAKPOINT);
    }

    // ── Action Helpers ─────────────────────────────────────────────────
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

    private void renderMeal() {
        // Title
        titleLabel.setText(mealType.getName());

        // Macro Preview
        DailyNutritionTotals totals = NutritionCalculationService.calculateMealNutritionTotals(currentMeal);
        macroPreview.setData(
                totals.calories(),
                totals.carbs(),
                totals.fat(),
                totals.protein()
        );

        // Items
        if (currentMeal == null || currentMeal.items().isEmpty()) {
            itemCountLabel.setText("0 items");
            itemsContainer.getChildren().clear();
            return;
        }

        itemCountLabel.setText(
                currentMeal.items().size() == 1
                        ? "1 item"
                        : currentMeal.items().size() + " items"
        );

        itemsContainer.getChildren().clear();

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

    private void initializeMacroPreview() {
        LoadedComponent<NutritionMacroPreviewController> preview = FxmlComponentLoader.load(AppConstants.Components.NUTRITION_MACRO_PREVIEW);

        macroPreview = preview.controller();

        macroPreviewContainer.getChildren().setAll(preview.root());
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
        }

        OverlayManager.close();

        LoadedComponent<AddToMealController> addFood = FxmlComponentLoader.load(AppConstants.Views.ADD_TO_MEAL);

        addFood.controller().setData(mealType, mealDate);

        addFood.controller().setOnCloseAction(() -> {
            if (onCloseAction != null) {
                onCloseAction.run();
            }
        });

        addFood.controller().setOnBackAction(changed -> {
            OverlayManager.close();
            OverlayManager.show(rootLayout);

            if (changed) {
                refreshMealDetails();
            }
        });

        OverlayManager.show(addFood.root());
    }

    // ── Responsive Helpers ─────────────────────────────────────────────────
    @Override
    public void updateWidthLayout(boolean narrow) {
        rootLayout.pseudoClassStateChanged(NARROW, narrow);

        if (narrow) {
            dialogContainer.prefWidthProperty().bind(rootLayout.widthProperty());
            dialogContainer.prefHeightProperty().bind(rootLayout.heightProperty());

            foodDetailsContainer.prefWidthProperty().bind(rootLayout.widthProperty());
            foodDetailsContainer.prefHeightProperty().bind(rootLayout.heightProperty());
        } else {
            dialogContainer.prefWidthProperty().unbind();
            dialogContainer.prefHeightProperty().unbind();

            foodDetailsContainer.prefWidthProperty().unbind();
            foodDetailsContainer.prefHeightProperty().unbind();

            dialogContainer.setPrefSize(
                    Region.USE_COMPUTED_SIZE,
                    Region.USE_COMPUTED_SIZE
            );

            foodDetailsContainer.setPrefSize(
                    Region.USE_COMPUTED_SIZE,
                    Region.USE_COMPUTED_SIZE
            );
        }
    }

    @Override
    public void updateHeightLayout(boolean shortLayout) {
        rootLayout.pseudoClassStateChanged(SHORT, shortLayout);
    }

    // ── Open Food Details ───────────────────────────────────────────────────
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

        foodDetailsContainer.getChildren().setAll(details.root());

        setVisible(dialogContainer, false);
        setVisible(foodDetailsContainer, true);
    }

    // ── Close Meal Details ───────────────────────────────────────────────────
    private void closeFoodDetails() {
        foodDetailsContainer.getChildren().clear();

        setVisible(foodDetailsContainer, false);
        setVisible(dialogContainer, true);
    }

    // ── Refresh Meal Details ───────────────────────────────────────────────────
    private void refreshMealDetails() {
        AsyncTaskRunner.run(
                () -> mealService.getMealsForDate(mealDate),

                meals -> {
                    MealResponse refreshedMeal = meals.stream()
                            .filter(item -> item.name().equals(mealType.getName()))
                            .findFirst()
                            .orElse(null);

                    setData(mealType, mealDate, refreshedMeal);
                    dataChanged = true;
                },

                exception -> log.error(
                        "Failed to refresh meal details.",
                        exception
                )
        );
    }

    // ── Update MealItem ───────────────────────────────────────────────────
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
                    closeFoodDetails();

                    if (mealChanged) {
                        removeMealItem(mealItem.id());
                    } else {
                        replaceMealItem(mealItem.id(), response);
                    }
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

    // ── Delete MealItem ───────────────────────────────────────────────────
    private void deleteMealItem(MealItemResponse mealItem, MealItemEditorController editor) {
        editor.setRemoveLoading(true);

        AsyncTaskRunner.run(
                () -> {
                    mealService.deleteMealItem(mealItem.id());
                    return null;
                },

                ignored -> {
                    dataChanged = true;
                    closeFoodDetails();
                    removeMealItem(mealItem.id());
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

    // ── MealItem Helpers ───────────────────────────────────────────────────
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
}