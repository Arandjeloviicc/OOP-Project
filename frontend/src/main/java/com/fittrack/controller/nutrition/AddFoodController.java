package com.fittrack.controller.nutrition;

import com.fittrack.api.nutrition.FoodApi;
import com.fittrack.api.nutrition.MealApi;
import com.fittrack.async.AsyncTaskRunner;
import com.fittrack.cache.FoodSearchCache;
import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.FormController;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.controller.nutrition.components.CreateFoodController;
import com.fittrack.controller.nutrition.components.FoodListItemController;
import com.fittrack.controller.nutrition.components.MealEditorController;
import com.fittrack.controller.nutrition.components.MealItemDetailsController;
import com.fittrack.dto.nutrition.meal.CreateMealRequest;
import com.fittrack.dto.nutrition.meal.item.AddMealItemRequest;
import com.fittrack.dto.nutrition.food.CreateFoodRequest;
import com.fittrack.dto.nutrition.food.FoodResponse;
import com.fittrack.dto.nutrition.meal.MealResponse;
import com.fittrack.dto.nutrition.meal.item.MealItemDraft;
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

public class AddFoodController extends FormController implements Initializable, ResponsiveLayout {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(AddFoodController.class);

    @Override
    protected Logger getLogger() { return log; }

    // Root
    @FXML private StackPane rootLayout;
    @FXML private VBox dialogContainer;

    // Food List
    @FXML private VBox foodListContainer;
    @FXML private Label titleLabel;
    @FXML private TextField searchField;
    @FXML private ToggleGroup foodCategoryGroup;
    @FXML private ToggleButton allFoodsButton;
    @FXML private ToggleButton myFoodsButton;
    @FXML private ToggleButton myMealsButton;
    @FXML private VBox createItemPanel;
    @FXML private Label createItemIcon;
    @FXML private Label createItemLabel;
    @FXML private VBox itemsContainer;

    // Details
    @FXML private StackPane foodDetailsContainer;

    // Create Item
    @FXML private StackPane createItemContainer;

    // Search Source Tabs
    private List<ToggleButton> searchTabs;

    // Actions
    private MealType mealType;
    private LocalDate mealDate;
    private Runnable onCloseAction;
    private Consumer<Boolean> onBackAction;
    private boolean dataChanged;

    // Active Card
    private MealEditorController activeMealEditor;

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
        if (activeMealEditor != null) {
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
        SearchSource source = (SearchSource) foodCategoryGroup.getSelectedToggle().getUserData();

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
        foodCategoryGroup.selectedToggleProperty().addListener(
                (obs, oldToggle, newToggle) -> {
                    if (newToggle == null) {
                        if (oldToggle != null) {
                            foodCategoryGroup.selectToggle(oldToggle);
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
                                (SearchSource) foodCategoryGroup
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

        foodCategoryGroup.selectToggle(allFoodsButton);

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

    // ── Food/Meal Loading Helpers ───────────────────────────────────────────────────
    private void loadSearchSource(SearchSource source) {
        switch (source) {
            case ALL -> {
                setVisible(createItemPanel, false);
                loadAllFoods(searchField.getText());
            }

            case MY_FOODS -> {
                showCreateFoodPanel();
                loadMyFoods(searchField.getText());
            }

            case MY_MEALS -> {
                showCreateMealPanel();
                loadMyMeals(searchField.getText());
            }
        }
    }

    private void showCreateFoodPanel() {
        setVisible(createItemPanel, true);

        createItemLabel.setText("Create a food");

        createItemPanel.getStyleClass().remove("create-meal");
        createItemPanel.getStyleClass().add("create-food");
    }

    private void showCreateMealPanel() {
        setVisible(createItemPanel, true);

        createItemLabel.setText("Create a meal");

        createItemPanel.getStyleClass().remove("create-food");
        createItemPanel.getStyleClass().add("create-meal");
    }

    // ── All Foods ───────────────────────────────────────────────────
    private void loadAllFoods(String search) {
        String cacheKey = normalizeSearch(search);

        if (FoodSearchCache.contains(cacheKey)) {
            showFoods(FoodSearchCache.get(cacheKey));
            return;
        }

        AsyncTaskRunner.run(
                () -> foodApi.searchFoods(search),

                foods -> {
                    FoodSearchCache.put(cacheKey, foods);

                    if (!cacheKey.equals(normalizeSearch(searchField.getText()))) {
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

                    SearchSource currentSource = (SearchSource) foodCategoryGroup.getSelectedToggle().getUserData();

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

                    SearchSource currentSource = (SearchSource) foodCategoryGroup.getSelectedToggle().getUserData();

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
        itemsContainer.getChildren().clear();

        for (FoodResponse food : foods) {
            LoadedComponent<FoodListItemController> item = FxmlComponentLoader.load(AppConstants.Components.FOOD_LIST_ITEM);

            item.controller().setData(
                    food.id(),
                    food.name(),
                    food.caloriesPerServing(),
                    food.servingSizeGrams()
            );

            item.controller().setOnAddAction(() -> openFoodDetails(food));

            itemsContainer.getChildren().add(item.root());
        }
    }

    private void showMeals(List<MealResponse> meals) {
        itemsContainer.getChildren().clear();

        for (MealResponse meal : meals) {
            int calories = MealService.calculateMealCalories(meal);

            String itemText = meal.items().size() == 1
                    ? "1 item"
                    : meal.items().size() + " items";

            Label label = new Label(
                    meal.name()
                            + " · "
                            + itemText
                            + " · "
                            + calories
                            + " cal"
            );

            label.getStyleClass().add("add-food-placeholder");

            itemsContainer.getChildren().add(label);
        }
    }

    // ── Food Details ───────────────────────────────────────────────────
    private void openFoodDetails(FoodResponse food) {
        LoadedComponent<MealItemDetailsController> details = FxmlComponentLoader.load(AppConstants.Components.MEAL_ITEM_DETAILS);

        if (activeMealEditor != null) {
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
                            quantityGrams
                    )
            );
        }

        details.controller().setOnCancelAction(
                this::closeFoodDetails
        );

        foodDetailsContainer.getChildren().setAll(details.root());

        setVisible(foodListContainer, false);
        setVisible(foodDetailsContainer, true);
    }

    private void closeFoodDetails() {
        foodDetailsContainer.getChildren().clear();

        setVisible(foodDetailsContainer, false);
        setVisible(foodListContainer, true);
    }

    private void returnToMealEditor() {
        foodDetailsContainer.getChildren().clear();

        setVisible(foodDetailsContainer, false);
        setVisible(foodListContainer, false);
        setVisible(createItemContainer, true);
    }

    // ── Meal Item Actions ──────────────────────────────────────────────
    private void addFoodToMeal(FoodResponse food, MealType selectedMeal, double quantityGrams) {
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
                    closeFoodDetails();
                },

                exception -> log.error(
                        "Failed to add food to meal.",
                        exception
                )
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

        activeMealEditor.addDraftItem(draftItem);

        returnToMealEditor();
    }

    // ── Create/Edit Food Action ──────────────────────────────────────────────
    private void openCreateFood() {
        LoadedComponent<CreateFoodController> createFood = FxmlComponentLoader.load(AppConstants.Components.CREATE_FOOD_CARD);

        createFood.controller().setOnCancelAction(
                this::closeCreateItem
        );

        createFood.controller().setOnCreateAction(request -> {
                    createFood(request);
        });

        createItemContainer.getChildren().setAll(createFood.root());

        setVisible(foodListContainer, false);
        setVisible(createItemContainer, true);
    }

    private void closeCreateItem() {
        createItemContainer.getChildren().clear();

        setVisible(createItemContainer, false);
        setVisible(foodListContainer, true);
    }

    private void createFood(CreateFoodRequest createFoodRequest) {
        Integer userId = UserSession.getInstance().getCurrentUser().id();

        AsyncTaskRunner.run(
                () -> foodApi.createFood(userId, createFoodRequest),

                food -> {
                    dataChanged = true;

                    FoodSearchCache.clear();
                    searchField.clear();

                    closeCreateItem();
                    loadMyFoods("");
                },

                exception -> log.error(
                        "Failed to create a food.",
                        exception
                )
        );
    }

    // ── Create/Edit Meal Action ──────────────────────────────────────────────
    private void openCreateMeal() {
        LoadedComponent<MealEditorController> editor = FxmlComponentLoader.load(AppConstants.Components.MEAL_EDITOR);

        activeMealEditor = editor.controller();
        activeMealEditor.setCreateMode();

        activeMealEditor.setOnCancelAction(() -> {
            activeMealEditor = null;

            closeCreateItem();

            searchField.clear();
            foodCategoryGroup.selectToggle(myMealsButton);
        });

        activeMealEditor.setOnAddFoodAction(
                this::openFoodSelectionForMealDraft
        );

        activeMealEditor.setOnSaveAction(
                this::createMeal
        );

        createItemContainer.getChildren().setAll(editor.root());

        setVisible(foodListContainer, false);
        setVisible(createItemContainer, true);
    }

    private void createMeal(CreateMealRequest request) {
        Integer userId = UserSession.getInstance().getCurrentUser().id();

        AsyncTaskRunner.run(
                () -> mealApi.createMyMeal(userId, request),

                meal -> {
                    activeMealEditor = null;

                    closeCreateItem();

                    searchField.clear();
                    foodCategoryGroup.selectToggle(myMealsButton);

                    loadMyMeals("");
                },

                exception -> log.error(
                        "Failed to create meal.",
                        exception
                )
        );
    }

    private void openFoodSelectionForMealDraft() {
        setVisible(createItemContainer, false);
        setVisible(foodDetailsContainer, false);
        setVisible(foodListContainer, true);

        titleLabel.setText("Add food to meal");

        searchField.clear();
        foodCategoryGroup.selectToggle(allFoodsButton);

        loadAllFoods("");
    }

    // ── Cache Helpers ──────────────────────────────────────────────
    private String normalizeSearch(String search) {
        return search == null
                ? ""
                : search.trim().toLowerCase(Locale.ROOT);
    }
}