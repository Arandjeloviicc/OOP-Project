package com.fittrack.controller.nutrition;

import com.fittrack.api.nutrition.FoodApi;
import com.fittrack.api.nutrition.MealApi;
import com.fittrack.async.AsyncTaskRunner;
import com.fittrack.cache.FoodSearchCache;
import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.FormController;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.controller.nutrition.components.*;
import com.fittrack.dto.nutrition.meal.CreateMealRequest;
import com.fittrack.dto.nutrition.meal.LogMealRequest;
import com.fittrack.dto.nutrition.meal.item.AddMealItemRequest;
import com.fittrack.dto.nutrition.food.CreateFoodRequest;
import com.fittrack.dto.nutrition.food.FoodResponse;
import com.fittrack.dto.nutrition.meal.MealResponse;
import com.fittrack.dto.nutrition.meal.item.MealItemDraft;
import com.fittrack.dto.nutrition.meal.item.UpdateSavedMealRequest;
import com.fittrack.model.nutrition.DailyNutritionTotals;
import com.fittrack.model.nutrition.MealType;
import com.fittrack.model.nutrition.SearchSource;
import com.fittrack.service.nutrition.MealService;
import com.fittrack.session.UserSession;
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

    @Override
    protected Logger getLogger() { return log; }

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

    // Actions
    private MealType mealType;
    private LocalDate mealDate;
    private Runnable onCloseAction;
    private Consumer<Boolean> onBackAction;
    private boolean dataChanged;

    // Active Meal Editor
    private SavedMealEditorController activeSavedMealEditor;

    // Search Helpers
    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(300));

    // Responsive Helpers
    private static final int NARROW_BREAKPOINT = 760;
    private static final int SHORT_BREAKPOINT = 650;
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");
    private static final PseudoClass SHORT = PseudoClass.getPseudoClass("short");

    // Api
    private final FoodApi foodApi = new FoodApi();
    private final MealApi mealApi = new MealApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
        if (activeSavedMealEditor != null) {
            returnToMealEditor();
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
        if (activeSavedMealEditor != null) {
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
                if (activeSavedMealEditor != null) {
                    setVisible(createPanel, false);
                } else {
                    showCreateFoodPanel();
                }

                loadMyFoods(searchField.getText());
            }

            case MY_MEALS -> {
                showCreateMealPanel();
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
                () -> foodApi.searchFoods(search),

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
        Integer userId = UserSession.getInstance().getCurrentUser().id();
        String normalizedSearch = normalizeSearch(search);

        AsyncTaskRunner.run(
                () -> foodApi.getMyFoods(userId, search),

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
        Integer userId = UserSession.getInstance().getCurrentUser().id();
        String normalizedSearch = normalizeSearch(search);

        AsyncTaskRunner.run(
                () -> mealApi.getMyMeals(userId, search),

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

        if (activeSavedMealEditor != null) {
            addFoodToMealDraft(food, quantityGrams);
            return;
        }

        addFoodToMeal(food, mealType, quantityGrams, onSuccess, onFailure);
    }

    private void showMeals(List<MealResponse> meals) {
        resultsContainer.getChildren().clear();

        for (MealResponse meal : meals) {
            LoadedComponent<SavedMealListItemController> item = FxmlComponentLoader.load(AppConstants.Components.SAVED_MEAL_LIST_ITEM);

            DailyNutritionTotals totals = MealService.calculateMealNutritionTotals(meal);

            item.controller().setData(
                    meal.name(),
                    totals.calories(),
                    totals.carbs(),
                    totals.fat(),
                    totals.protein()
            );

            item.controller().setOnEditAction(
                    () -> openSavedMealEditor(meal)
            );

            item.controller().setOnAddAction(
                    () -> logMyMeal(
                            meal,
                            item.controller()::showAddSuccess,
                            item.controller()::resetAddFeedback
                    )
            );

            resultsContainer.getChildren().add(item.root());
        }
    }

    private void openSavedMealEditor(MealResponse meal) {
        LoadedComponent<SavedMealEditorController> editor = FxmlComponentLoader.load(AppConstants.Components.SAVED_MEAL_EDITOR);

        activeSavedMealEditor = editor.controller();
        activeSavedMealEditor.setEditMode(meal);

        activeSavedMealEditor.setOnCancelAction(() -> {
            activeSavedMealEditor = null;

            closeEditor();
            resetSearchSource(myMealsButton);
        });

        activeSavedMealEditor.setOnAddFoodAction(
                this::openFoodSelectionForMealDraft
        );

        activeSavedMealEditor.setOnUpdateAction(
                request -> updateMeal(meal.id(), request)
        );

        activeSavedMealEditor.setOnDeleteAction(
                () -> deleteMeal(meal.id())
        );

        editorContainer.getChildren().setAll(editor.root());

        setVisible(selectionContainer, false);
        setVisible(editorContainer, true);
    }

    private void openSaveAsMealEditor(MealResponse sourceMeal) {
        LoadedComponent<SavedMealEditorController> editor = FxmlComponentLoader.load(AppConstants.Components.SAVED_MEAL_EDITOR);

        activeSavedMealEditor = editor.controller();

        activeSavedMealEditor.setCreateMode(sourceMeal);

        activeSavedMealEditor.setOnCancelAction(() -> {
            activeSavedMealEditor = null;
            OverlayManager.close();
        });

        activeSavedMealEditor.setOnAddFoodAction(
                this::openFoodSelectionForMealDraft
        );

        activeSavedMealEditor.setOnCreateAction(
                request -> createSavedMeal(
                        request,
                        OverlayManager::close
                )
        );

        editorContainer.getChildren().setAll(editor.root());

        setVisible(selectionContainer, false);
        setVisible(itemDetailsContainer, false);
        setVisible(editorContainer, true);
    }

    private void updateMeal(Integer mealId, UpdateSavedMealRequest request) {
        Integer userId = UserSession.getInstance().getCurrentUser().id();

        activeSavedMealEditor.setSubmitting(true);

        AsyncTaskRunner.run(
                () -> mealApi.updateMyMeal(userId, mealId, request),

                updatedMeal -> {
                    activeSavedMealEditor = null;

                    closeEditor();
                    resetSearchSource(myMealsButton);
                },

                exception -> {
                    if (activeSavedMealEditor != null) {
                        activeSavedMealEditor.setSubmitting(false);
                    }

                    log.error(
                            "Failed to update meal.",
                            exception
                    );
                }
        );
    }

    private void deleteMeal(Integer mealId) {
        Integer userId = UserSession.getInstance().getCurrentUser().id();

        activeSavedMealEditor.setSubmitting(true);

        AsyncTaskRunner.run(
                () -> {
                    mealApi.deleteMyMeal(userId, mealId);
                    return null;
                },

                ignored -> {
                    activeSavedMealEditor = null;

                    closeEditor();
                    resetSearchSource(myMealsButton);
                },

                exception -> {
                    if (activeSavedMealEditor != null) {
                        activeSavedMealEditor.setSubmitting(false);
                    }

                    log.error(
                            "Failed to delete meal.",
                            exception
                    );
                }
        );
    }

    private void logMyMeal(MealResponse meal, Runnable onSuccess, Runnable onFailure) {
        Integer userId = UserSession.getInstance().getCurrentUser().id();

        LogMealRequest request = new LogMealRequest(mealDate, mealType.getName());

        AsyncTaskRunner.run(
                () -> {
                    mealApi.logMyMeal(userId, meal.id(), request);
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
        LoadedComponent<MealItemEditorController> details = FxmlComponentLoader.load(AppConstants.Components.MEAL_ITEM_EDITOR);

        if (activeSavedMealEditor != null) {
            details.controller().setDraftData(food);
            details.controller().setCaption("Add food");
            details.controller().setConfirmButtonText("Add food");

            details.controller().setOnConfirmAction(
                    quantityGrams -> addFoodToMealDraft(
                            food,
                            quantityGrams
                    )
            );

        } else {
            details.controller().setData(food, mealType);
            details.controller().setCaption("Add food");
            details.controller().setConfirmButtonText("Add to meal");

            details.controller().setOnConfirmAction(
                    quantityGrams -> addFoodToMeal(
                            food,
                            details.controller().getSelectedMealType(),
                            quantityGrams,
                            this::closeFoodDetails,
                            null
                    )
            );
        }

        details.controller().setOnCancelAction(
                this::closeFoodDetails
        );

        itemDetailsContainer.getChildren().setAll(details.root());

        setVisible(selectionContainer, false);
        setVisible(itemDetailsContainer, true);
    }

    private void closeFoodDetails() {
        itemDetailsContainer.getChildren().clear();

        setVisible(itemDetailsContainer, false);
        setVisible(selectionContainer, true);
    }

    private void returnToMealEditor() {
        itemDetailsContainer.getChildren().clear();

        setVisible(myMealsButton, true);

        setVisible(itemDetailsContainer, false);
        setVisible(selectionContainer, false);
        setVisible(editorContainer, true);
    }

    // ── Meal Item Actions ──────────────────────────────────────────────
    private void addFoodToMeal(FoodResponse food, MealType selectedMeal, double quantityGrams, Runnable onSuccess, Runnable onFailure) {
        Integer userId = UserSession.getInstance().getCurrentUser().id();

        AddMealItemRequest request = new AddMealItemRequest(
                mealDate,
                selectedMeal.getName(),
                food.id(), quantityGrams
        );

        AsyncTaskRunner.run(
                () -> mealApi.addMealItem(userId, request),

                response -> {
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

        activeSavedMealEditor.addDraftItem(draftItem);

        returnToMealEditor();
    }

    // ── Create/Edit Food Action ──────────────────────────────────────────────
    private void openCreateFood() {
        LoadedComponent<FoodEditorController> editor = FxmlComponentLoader.load(AppConstants.Components.FOOD_EDITOR);

        editor.controller().setCreateMode();

        editor.controller().setOnCancelAction(
                this::closeEditor
        );

        editor.controller().setOnCreateAction(
                request -> createFood(
                        request,
                        editor.controller()
                )
        );
        editorContainer.getChildren().setAll(editor.root());

        setVisible(selectionContainer, false);
        setVisible(editorContainer, true);
    }

    private void closeEditor() {
        editorContainer.getChildren().clear();

        setVisible(editorContainer, false);
        setVisible(selectionContainer, true);
    }

    private void createFood(CreateFoodRequest createFoodRequest, FoodEditorController editor) {
        Integer userId = UserSession.getInstance().getCurrentUser().id();

        editor.setSubmitting(true);

        AsyncTaskRunner.run(
                () -> foodApi.createFood(userId, createFoodRequest),

                food -> {
                    FoodSearchCache.clear();

                    closeEditor();
                    resetSearchSource(myFoodsButton);
                },

                exception -> {
                    editor.setSubmitting(false);

                    log.error(
                            "Failed to create a food.",
                            exception
                    );
                }
        );
    }

    // ── Create/Edit Meal Action ──────────────────────────────────────────────
    private void openCreateMeal() {
        LoadedComponent<SavedMealEditorController> editor = FxmlComponentLoader.load(AppConstants.Components.SAVED_MEAL_EDITOR);

        activeSavedMealEditor = editor.controller();
        activeSavedMealEditor.setCreateMode();

        activeSavedMealEditor.setOnCancelAction(() -> {
            activeSavedMealEditor = null;

            closeEditor();
            resetSearchSource(myMealsButton);
        });

        activeSavedMealEditor.setOnAddFoodAction(
                this::openFoodSelectionForMealDraft
        );

        activeSavedMealEditor.setOnCreateAction(
                this::createSavedMeal
        );

        editorContainer.getChildren().setAll(editor.root());

        setVisible(selectionContainer, false);
        setVisible(editorContainer, true);
    }

    private void createSavedMeal(CreateMealRequest request) {
        createSavedMeal(request, this::returnToMyMeals);
    }

    private void returnToMyMeals() {
        closeEditor();
        resetSearchSource(myMealsButton);
    }

    private void createSavedMeal(CreateMealRequest request, Runnable onSuccess) {
        Integer userId = UserSession.getInstance().getCurrentUser().id();

        activeSavedMealEditor.setSubmitting(true);

        AsyncTaskRunner.run(
                () -> mealApi.createMyMeal(userId, request),

                meal -> {
                    activeSavedMealEditor = null;

                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                },

                exception -> {
                    if (activeSavedMealEditor != null) {
                        activeSavedMealEditor.setSubmitting(false);
                    }

                    log.error(
                            "Failed to create meal.",
                            exception
                    );
                }
        );
    }

    private void openFoodSelectionForMealDraft() {
        setVisible(editorContainer, false);
        setVisible(itemDetailsContainer, false);
        setVisible(selectionContainer, true);

        setVisible(myMealsButton, false);

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

    // ── Cache Helpers ──────────────────────────────────────────────
    private String normalizeSearch(String search) {
        return search == null
                ? ""
                : search.trim().toLowerCase(Locale.ROOT);
    }
}