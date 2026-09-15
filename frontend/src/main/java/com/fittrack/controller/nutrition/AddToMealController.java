package com.fittrack.controller.nutrition;

import com.fittrack.async.AsyncTaskRunner;
import com.fittrack.cache.FoodSearchCache;
import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.FormController;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.controller.nutrition.components.*;
import com.fittrack.coordinator.nutrition.AddToMealCoordinator;
import com.fittrack.dto.nutrition.meal.CreateMealRequest;
import com.fittrack.dto.nutrition.meal.LogSavedMealRequest;
import com.fittrack.dto.nutrition.meal.item.AddMealItemRequest;
import com.fittrack.dto.nutrition.food.CreateFoodRequest;
import com.fittrack.dto.nutrition.food.FoodResponse;
import com.fittrack.dto.nutrition.meal.MealResponse;
import com.fittrack.dto.nutrition.meal.item.MealItemDraft;
import com.fittrack.dto.nutrition.meal.UpdateSavedMealRequest;
import com.fittrack.model.nutrition.DailyNutritionTotals;
import com.fittrack.model.nutrition.MealType;
import com.fittrack.model.nutrition.SearchSource;
import com.fittrack.service.nutrition.FoodService;
import com.fittrack.service.nutrition.MealService;
import com.fittrack.service.nutrition.NutritionCalculationService;
import com.fittrack.ui.FxmlComponentLoader;
import com.fittrack.ui.LoadedComponent;
import com.fittrack.ui.OverlayManager;
import javafx.animation.PauseTransition;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

public class AddToMealController extends FormController implements Initializable, ResponsiveLayout {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(AddToMealController.class);

    // Root
    @FXML private StackPane rootLayout;

    // Selection
    @FXML private VBox selectionContainer;
    @FXML private VBox selectionDialog;

    @FXML private Label titleLabel;
    @FXML private TextField searchField;

    @FXML private ToggleGroup searchSourceGroup;
    @FXML private ToggleButton allFoodsButton;
    @FXML private ToggleButton myFoodsButton;
    @FXML private ToggleButton myMealsButton;

    @FXML private VBox createPanel;
    @FXML private Label createPanelLabel;

    @FXML private VBox resultsContainer;

    // Item Details
    @FXML private StackPane itemDetailsContainer;

    // Editor
    @FXML private StackPane editorContainer;

    // Search Source Tabs
    private List<ToggleButton> searchTabs;

    // Coordinator
    private AddToMealCoordinator coordinator;

    // Actions
    private MealType mealType;
    private LocalDate mealDate;
    private Runnable onCloseAction;
    private Consumer<Boolean> onBackAction;
    private boolean dataChanged;

    // Search Helpers
    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(300));

    // Responsive Helpers
    private static final int NARROW_BREAKPOINT = 760;
    private static final int SHORT_BREAKPOINT = 650;
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");
    private static final PseudoClass SHORT = PseudoClass.getPseudoClass("short");

    // Service
    private final FoodService foodService = new FoodService();
    private final MealService mealService = new MealService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Coordinator
        coordinator = new AddToMealCoordinator(selectionContainer, itemDetailsContainer, editorContainer);

        // Responsive Initialize
        initializeResponsiveWidthLayout(rootLayout, NARROW_BREAKPOINT);
        initializeResponsiveHeightLayout(rootLayout, SHORT_BREAKPOINT);

        initializeSearchTabs();

        addListeners();
    }

    // ── Set Data ────────────────────────────────────────────
    public void setData(MealType mealType, LocalDate mealDate) {
        this.mealType = mealType;
        this.mealDate = mealDate;
        this.dataChanged = false;

        titleLabel.setText("Add to " + mealType.getName());

        loadAllFoods("");
    }

    public void setSaveAsMealData(MealResponse sourceMeal) {
        this.dataChanged = false;

        openSaveAsMealEditor(sourceMeal);
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
    private void handleBack() {
        if (coordinator.hasActiveSavedMealEditor()) {
            coordinator.returnToMealEditor();
            return;
        }

        if (onBackAction != null) {
            onBackAction.accept(dataChanged);
        } else {
            OverlayManager.close();
        }
    }

    @FXML
    private void handleCreateItem() {
        if (coordinator.hasActiveSavedMealEditor()) {
            return;
        }

        SearchSource source = (SearchSource) searchSourceGroup.getSelectedToggle().getUserData();

        switch (source) {
            case MY_FOODS -> openCreateFood();
            case MY_MEALS -> openCreateMeal();
        }
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    public void setOnBackAction(Consumer<Boolean> onBackAction) {
        this.onBackAction = onBackAction;
    }

    // ── Initialize Helpers ─────────────────────────────────────────────────
    private void addListeners() {
        // Search Toggle Button
        searchSourceGroup.selectedToggleProperty().addListener(
                (obs, oldToggle, newToggle) -> {
                    if (newToggle == null) {
                        if (oldToggle != null) {
                            searchSourceGroup.selectToggle(oldToggle);
                        }
                        return;
                    }

                    SearchSource source = (SearchSource) newToggle.getUserData();

                    loadSearchSource(source);
                }
        );

        // Seacrh Field
        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    searchDebounce.stop();

                    searchDebounce.setOnFinished(event -> {
                        SearchSource source =
                                (SearchSource) searchSourceGroup
                                        .getSelectedToggle()
                                        .getUserData();

                        loadSearchSource(source);
                    });

                    searchDebounce.playFromStart();
                }
        );
    }

    private void initializeSearchTabs() {
        searchTabs = List.of(
                allFoodsButton,
                myFoodsButton,
                myMealsButton
        );

        allFoodsButton.setUserData(SearchSource.ALL);
        myFoodsButton.setUserData(SearchSource.MY_FOODS);
        myMealsButton.setUserData(SearchSource.MY_MEALS);

        searchSourceGroup.selectToggle(allFoodsButton);

        updateTabsLayout();
    }

    private void updateTabsLayout() {
        for (ToggleButton tab : searchTabs) {
            HBox.setHgrow(tab, Priority.ALWAYS);
            tab.setMaxWidth(Double.MAX_VALUE);
        }
    }

    // ── Responsive Helpers ─────────────────────────────────────────────────
    @Override
    public void updateWidthLayout(boolean narrow) {
        rootLayout.pseudoClassStateChanged(NARROW, narrow);

        if (narrow) {
            selectionDialog.prefWidthProperty().bind(rootLayout.widthProperty());
            selectionDialog.prefHeightProperty().bind(rootLayout.heightProperty());

            itemDetailsContainer.prefWidthProperty().bind(rootLayout.widthProperty());
            itemDetailsContainer.prefHeightProperty().bind(rootLayout.heightProperty());
        } else {
            selectionDialog.prefWidthProperty().unbind();
            selectionDialog.prefHeightProperty().unbind();

            itemDetailsContainer.prefWidthProperty().unbind();
            itemDetailsContainer.prefHeightProperty().unbind();

            selectionDialog.setPrefSize(
                    Region.USE_COMPUTED_SIZE,
                    Region.USE_COMPUTED_SIZE
            );

            itemDetailsContainer.setPrefSize(
                    Region.USE_COMPUTED_SIZE,
                    Region.USE_COMPUTED_SIZE
            );
        }
    }

    @Override
    public void updateHeightLayout(boolean shortLayout) {
        rootLayout.pseudoClassStateChanged(SHORT, shortLayout);
    }

    // ── Selection Loading ───────────────────────────────────────────────────
    private void loadSearchSource(SearchSource source) {
        switch (source) {
            case ALL -> {
                setVisible(createPanel, false);
                loadAllFoods(searchField.getText());
            }

            case MY_FOODS -> {
                if (coordinator.hasActiveSavedMealEditor()) {
                    setVisible(createPanel, false);
                } else {
                    showCreateFoodPanel();
                }

                loadMyFoods(searchField.getText());
            }

            case MY_MEALS -> {
                if (coordinator.hasActiveSavedMealEditor()) {
                    setVisible(createPanel, false);
                } else {
                    showCreateMealPanel();
                }

                loadMyMeals(searchField.getText());
            }
        }
    }

    private void showCreateFoodPanel() {
        setVisible(createPanel, true);

        createPanelLabel.setText("Create a food");

        setCreatePanelStyle("create-food");
    }

    private void showCreateMealPanel() {
        setVisible(createPanel, true);

        createPanelLabel.setText("Create a meal");

        setCreatePanelStyle("create-meal");
    }

    private void setCreatePanelStyle(String styleClass) {
        createPanel.getStyleClass().removeAll(
                "create-food",
                "create-meal"
        );

        createPanel.getStyleClass().add(styleClass);
    }

    // ── All Foods ───────────────────────────────────────────────────
    private void loadAllFoods(String search) {
        String cacheKey = normalizeSearch(search);

        if (FoodSearchCache.contains(cacheKey)) {
            SearchSource currentSource = (SearchSource) searchSourceGroup.getSelectedToggle().getUserData();

            if (currentSource == SearchSource.ALL) {
                showFoods(FoodSearchCache.get(cacheKey));
            }

            return;
        }

        AsyncTaskRunner.run(
                () -> foodService.searchAllFoods(search),

                foods -> {
                    FoodSearchCache.put(cacheKey, foods);

                    if (!cacheKey.equals(normalizeSearch(searchField.getText()))) {
                        return;
                    }

                    SearchSource currentSource = (SearchSource) searchSourceGroup.getSelectedToggle().getUserData();

                    if (currentSource != SearchSource.ALL) {
                        return;
                    }

                    showFoods(foods);
                },

                exception -> log.error(
                        "Failed to load foods.",
                        exception
                )
        );
    }

    // ── My Foods ───────────────────────────────────────────────────
    private void loadMyFoods(String search) {
        String normalizedSearch = normalizeSearch(search);

        AsyncTaskRunner.run(
                () -> foodService.searchMyFoods(search),

                foods -> {
                    if (!normalizedSearch.equals(normalizeSearch(searchField.getText()))) {
                        return;
                    }

                    SearchSource currentSource = (SearchSource) searchSourceGroup.getSelectedToggle().getUserData();

                    if (currentSource != SearchSource.MY_FOODS) {
                        return;
                    }

                    showFoods(foods);
                },

                exception -> log.error(
                        "Failed to load user's foods.",
                        exception
                )
        );
    }

    // ── My Meals ───────────────────────────────────────────────────
    private void loadMyMeals(String search) {
        String normalizedSearch = normalizeSearch(search);

        AsyncTaskRunner.run(
                () -> mealService.searchMyMeals(search),

                meals -> {
                    if (!normalizedSearch.equals(normalizeSearch(searchField.getText()))) {
                        return;
                    }

                    SearchSource currentSource = (SearchSource) searchSourceGroup.getSelectedToggle().getUserData();

                    if (currentSource != SearchSource.MY_MEALS) {
                        return;
                    }

                    showMeals(meals);
                },

                exception -> log.error(
                        "Failed to load user's meals.",
                        exception
                )
        );
    }

    private void showFoods(List<FoodResponse> foods) {
        resultsContainer.getChildren().clear();

        for (FoodResponse food : foods) {
            LoadedComponent<FoodListItemController> item = FxmlComponentLoader.load(AppConstants.Components.FOOD_LIST_ITEM);

            item.controller().setData(
                    food.name(),
                    food.caloriesPerServing(),
                    food.servingSizeGrams()
            );

            item.controller().setOnOpenAction(
                    () -> openFoodDetails(food)
            );

            item.controller().setOnAddAction(
                    () -> quickAddFood(
                            food,
                            item.controller()::showAddSuccess,
                            item.controller()::resetAddFeedback
                    )
            );

            resultsContainer.getChildren().add(item.root());
        }
    }

    private void quickAddFood(FoodResponse food, Runnable onSuccess, Runnable onFailure) {
        double quantityGrams = food.servingSizeGrams();

        if (coordinator.hasActiveSavedMealEditor()) {
            addFoodToMealDraft(food, quantityGrams);
            return;
        }

        addFoodToMeal(food, mealType, quantityGrams, onSuccess, onFailure);
    }

    private void showMeals(List<MealResponse> meals) {
        resultsContainer.getChildren().clear();

        for (MealResponse meal : meals) {

            // If we are editing a saved meal, don't show that meal in My Meals
            if (coordinator.hasActiveSavedMealEditor()) {
                if (coordinator.isEditingSavedMeal(meal.id())) {
                    continue;
                }

                if (meal.items().isEmpty()) {
                    continue;
                }
            }

            LoadedComponent<SavedMealListItemController> item = FxmlComponentLoader.load(AppConstants.Components.SAVED_MEAL_LIST_ITEM);

            DailyNutritionTotals totals = NutritionCalculationService.calculateMealNutritionTotals(meal);

            item.controller().setData(
                    meal.name(),
                    totals.calories(),
                    totals.carbs(),
                    totals.fat(),
                    totals.protein()
            );

            if (coordinator.hasActiveSavedMealEditor()) {
                item.controller().setOnAddAction(
                        () -> addMealToMealDraft(meal)
                );
            } else {
                item.controller().setOnEditAction(
                        () -> openSavedMealEditor(meal)
                );

                item.controller().setOnAddAction(
                        () -> logSavedMeal(
                                meal,
                                item.controller()::showAddSuccess,
                                item.controller()::resetAddFeedback
                        )
                );
            }

            resultsContainer.getChildren().add(item.root());
        }
    }

    private void addMealToMealDraft(MealResponse meal) {
        coordinator.addDraftItems(
                NutritionCalculationService.createDraftItems(meal)
        );

        coordinator.returnToMealEditor();
    }

    private void openSavedMealEditor(MealResponse meal) {
        coordinator.openSavedMealEditor(
                meal,

                this::restoreMyMealsIfNeeded,

                this::openFoodSelectionForMealDraft,

                request -> updateMeal(
                        meal.id(),
                        request
                ),

                () -> deleteMeal(meal.id())
        );
    }

    private void openSaveAsMealEditor(MealResponse sourceMeal) {
        coordinator.openSaveAsMealEditor(
                sourceMeal,

                OverlayManager::close,

                this::openFoodSelectionForMealDraft,

                request -> createSavedMeal(
                        request,
                        () -> {
                            coordinator.closeEditor();
                            OverlayManager.close();
                        }
                )
        );
    }

    private void updateMeal(Integer mealId, UpdateSavedMealRequest request) {
        AsyncTaskRunner.run(
                () -> {
                    mealService.updateSavedMeal(mealId, request);

                    return null;
                },

                ignored -> {
                    coordinator.closeEditor();
                    resetSearchSource(myMealsButton);
                },

                exception -> {
                    coordinator.setSavedMealEditorSaving(false);

                    log.error(
                            "Failed to update meal.",
                            exception
                    );
                }
        );
    }

    private void deleteMeal(Integer mealId) {
        AsyncTaskRunner.run(
                () -> {
                    mealService.deleteSavedMeal(mealId);
                    return null;
                },

                ignored -> {
                    coordinator.closeEditor();
                    resetSearchSource(myMealsButton);
                },

                exception -> {
                    coordinator.setSavedMealEditorDeleting(false);

                    log.error(
                            "Failed to delete meal.",
                            exception
                    );
                }
        );
    }

    private void logSavedMeal(MealResponse meal, Runnable onSuccess, Runnable onFailure) {
        LogSavedMealRequest request = new LogSavedMealRequest(mealDate, mealType.getName());

        AsyncTaskRunner.run(
                () -> {
                    mealService.logSavedMeal(meal.id(), request);
                    return null;
                },

                ignored -> {
                    dataChanged = true;

                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                },

                exception -> {
                    if (onFailure != null) {
                        onFailure.run();
                    }

                    log.error(
                            "Failed to log meal.",
                            exception
                    );
                }
        );
    }

    // ── Food Details ───────────────────────────────────────────────────
    private void openFoodDetails(FoodResponse food) {
        coordinator.openFoodDetails(
                food,
                mealType,

                quantityGrams -> addFoodToMealDraft(
                        food,
                        quantityGrams
                ),

                (selectedMeal, quantityGrams) -> addFoodToMeal(
                        food,
                        selectedMeal,
                        quantityGrams,
                        coordinator::closeFoodDetails,
                        coordinator::resetMealItemAdding
                )
        );
    }

    // ── Meal Item Actions ──────────────────────────────────────────────
    private void addFoodToMeal(FoodResponse food, MealType selectedMeal, double quantityGrams, Runnable onSuccess, Runnable onFailure) {
        AddMealItemRequest request = new AddMealItemRequest(
                mealDate,
                selectedMeal.getName(),
                food.id(), quantityGrams
        );

        AsyncTaskRunner.run(
                () -> {
                    mealService.addMealItem(request);
                    return null;
                },

                ignored -> {
                    dataChanged = true;

                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                },

                exception -> {
                    if (onFailure != null) {
                        onFailure.run();
                    }

                    log.error(
                            "Failed to add food to meal.",
                            exception
                    );
                }
        );
    }

    private void addFoodToMealDraft(FoodResponse food, double quantityGrams) {
        MealItemDraft draftItem = new MealItemDraft(
                null,
                food.id(),
                food.name(),
                food.brand(),
                quantityGrams,
                food.servingSizeGrams(),
                food.caloriesPerServing(),
                food.proteinPerServing(),
                food.carbsPerServing(),
                food.fatPerServing()
        );

        coordinator.addDraftItem(draftItem);

        coordinator.returnToMealEditor();
    }

    // ── Create/Edit Food Action ──────────────────────────────────────────────
    private void openCreateFood() {
        coordinator.openCreateFood(
                this::createFood
        );
    }

    private void createFood(CreateFoodRequest createFoodRequest) {
        AsyncTaskRunner.run(
                () -> foodService.createFood(createFoodRequest),

                food -> {
                    FoodSearchCache.clear();

                    coordinator.closeEditor();
                    resetSearchSource(myFoodsButton);
                },

                exception -> {
                    coordinator.setFoodEditorSubmitting(false);

                    log.error(
                            "Failed to create a food.",
                            exception
                    );
                }
        );
    }

    // ── Create/Edit Meal Action ──────────────────────────────────────────────
    private void openCreateMeal() {
        coordinator.openCreateMeal(
                this::restoreMyMealsIfNeeded,

                this::openFoodSelectionForMealDraft,

                this::createSavedMeal
        );
    }

    private void returnToMyMeals() {
        coordinator.closeEditor();
        resetSearchSource(myMealsButton);
    }

    private void createSavedMeal(CreateMealRequest request) {
        createSavedMeal(request, this::returnToMyMeals);
    }

    private void createSavedMeal(CreateMealRequest request, Runnable onSuccess) {
        AsyncTaskRunner.run(
                () -> {
                    mealService.createSavedMeal(request);
                    return null;
                },

                ignored -> {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                },

                exception -> {
                    coordinator.setSavedMealEditorSaving(false);

                    log.error(
                            "Failed to create meal.",
                            exception
                    );
                }
        );
    }

    private void openFoodSelectionForMealDraft() {
        coordinator.showSelection();

        titleLabel.setText("Add to meal");

        resetSearchSource(allFoodsButton);
    }

    // ── Search Helpers ──────────────────────────────────────────────
    private void resetSearchSource(ToggleButton targetButton) {
        searchField.clear();
        searchDebounce.stop();

        if (searchSourceGroup.getSelectedToggle() == targetButton) {
            SearchSource source = (SearchSource) targetButton.getUserData();

            loadSearchSource(source);
            return;
        }

        searchSourceGroup.selectToggle(targetButton);
    }

    private void restoreMyMealsIfNeeded() {
        if (searchSourceGroup.getSelectedToggle() == myMealsButton) {
            return;
        }

        resetSearchSource(myMealsButton);
    }

    // ── Cache Helpers ──────────────────────────────────────────────
    private String normalizeSearch(String search) {
        return search == null
                ? ""
                : search.trim().toLowerCase(Locale.ROOT);
    }
}