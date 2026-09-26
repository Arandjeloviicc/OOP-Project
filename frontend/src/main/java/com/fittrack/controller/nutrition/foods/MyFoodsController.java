package com.fittrack.controller.nutrition.foods;

import com.fittrack.async.AsyncTaskRunner;
import com.fittrack.cache.FoodSearchCache;
import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.NavigableController;
import com.fittrack.controller.common.Refreshable;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.controller.nutrition.foods.components.MyFoodCardController;
import com.fittrack.coordinator.nutrition.foods.MyFoodsCoordinator;
import com.fittrack.dto.nutrition.food.FoodRequest;
import com.fittrack.dto.nutrition.food.FoodResponse;
import com.fittrack.service.nutrition.FoodService;
import com.fittrack.ui.form.AppSearchFieldConfigurer;
import com.fittrack.ui.loader.FxmlComponentLoader;
import com.fittrack.ui.loader.LoadedComponent;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class MyFoodsController extends NavigableController implements Initializable, Refreshable, ResponsiveLayout {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(MyFoodsController.class);

    // Containers
    @FXML private StackPane rootLayout;
    @FXML private VBox contentContainer;
    @FXML private VBox foodsContainer;

    // Scroll
    @FXML private ScrollPane foodsScroll;

    // Add Button
    @FXML private Button addFoodButton;

    // Search
    @FXML private TextField searchField;
    @FXML private Label noResultsLabel;

    // No Foods
    @FXML private VBox emptyState;

    // Data
    private List<FoodResponse> foods;

    // Responsive
    private static final int NARROW_BREAKPOINT = 500;
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");

    // Coordinator
    private final MyFoodsCoordinator coordinator = new MyFoodsCoordinator();

    // Service
    private final FoodService foodService = new FoodService();

    // ── Initialization ──────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize Responsive
        initializeResponsiveWidthLayout(rootLayout, NARROW_BREAKPOINT);

        // Initialize search and load foods
        initializeSearch();
        loadFoods();
    }

    private void initializeSearch() {
        AppSearchFieldConfigurer.configure(
                searchField,
                this::showFilteredFoods
        );
    }

    // ── Load Foods ──────────────────────────────────────────────
    private void loadFoods() {
        AsyncTaskRunner.run(
                () -> foodService.searchMyFoods(""),

                loadedFoods -> {
                    foods = new ArrayList<>(loadedFoods);

                    sortFoods();
                    updateView();
                },

                exception -> log.error(
                        "Failed to load user's foods.",
                        exception
                )
        );
    }

    private void sortFoods() {
        foods.sort(
                Comparator.comparing(
                        FoodResponse::name,
                        String.CASE_INSENSITIVE_ORDER
                )
        );
    }

    private void updateView() {
        boolean empty =
                foods == null || foods.isEmpty();

        setVisible(contentContainer, !empty);
        setVisible(emptyState, empty);

        if (!empty) {
            showFilteredFoods(searchField.getText());
        }
    }

    private void showFilteredFoods(String search) {
        if (foods == null) {
            return;
        }

        String normalizedSearch = AppSearchFieldConfigurer.normalize(search);

        if (normalizedSearch.isEmpty()) {
            showFoods(foods);
            return;
        }

        List<FoodResponse> filteredFoods =
                foods.stream()
                        .filter(food ->
                                matchesSearch(
                                        food,
                                        normalizedSearch
                                )
                        )
                        .toList();

        showFoods(filteredFoods);
    }

    private boolean matchesSearch(FoodResponse food, String search) {
        String name = AppSearchFieldConfigurer.normalize(food.name());

        String brand = AppSearchFieldConfigurer.normalize(food.brand());

        return name.contains(search) || brand.contains(search);
    }

    private void showFoods(List<FoodResponse> visibleFoods) {
        foodsContainer.getChildren().clear();

        boolean noResults = visibleFoods.isEmpty();

        noResultsLabel.setVisible(noResults);
        noResultsLabel.setManaged(noResults);

        foodsScroll.setVisible(!noResults);
        foodsScroll.setManaged(!noResults);

        for (FoodResponse food : visibleFoods) {
            LoadedComponent<MyFoodCardController> component = FxmlComponentLoader.load(AppConstants.Components.MY_FOOD_CARD);

            component.controller().setData(food);

            component.controller().setOnEditAction(
                    () -> openEditFood(food)
            );

            component.controller().setOnDeleteAction(
                    () -> openDeleteConfirmation(food)
            );

            foodsContainer.getChildren().add(component.root());
        }
    }

    // ── Create Food ──────────────────────────────────────────────
    private void openCreateFood() {
        coordinator.openCreateFoodEditor(
                this::createFood
        );
    }

    private void createFood(FoodRequest request) {
        AsyncTaskRunner.run(
                () -> foodService.createFood(request),

                createdFood -> {
                    foods.add(createdFood);

                    FoodSearchCache.clear();

                    sortFoods();

                    searchField.clear();
                    updateView();

                    coordinator.closeFoodEditor();
                },

                exception -> {
                    coordinator.setFoodEditorSaving(false);

                    coordinator.showFoodEditorSaveError(AppConstants.Messages.CREATE_FOOD_ERROR);

                    log.error(
                            "Failed to create food.",
                            exception
                    );
                }
        );
    }

    // ── Edit Food ──────────────────────────────────────────────
    private void openEditFood(FoodResponse food) {
        coordinator.openEditFoodEditor(
                food,
                request -> updateFood(
                        food.id(),
                        request
                )
        );
    }

    private void updateFood(Integer foodId, FoodRequest request) {
        AsyncTaskRunner.run(
                () -> foodService.updateFood(foodId, request),

                updatedFood -> {
                    replaceFood(updatedFood);

                    FoodSearchCache.clear();

                    sortFoods();
                    updateView();

                    coordinator.closeFoodEditor();
                },

                exception -> {
                    coordinator.setFoodEditorSaving(false);

                    coordinator.showFoodEditorSaveError(AppConstants.Messages.UPDATE_FOOD_ERROR);

                    log.error(
                            "Failed to update food.",
                            exception
                    );
                }
        );
    }

    private void replaceFood(FoodResponse updatedFood) {
        for (int i = 0; i < foods.size(); i++) {
            if (foods.get(i).id().equals(updatedFood.id())) {
                foods.set(i, updatedFood);
                return;
            }
        }
    }

    // ── Delete Food ──────────────────────────────────────────────
    private void openDeleteConfirmation(FoodResponse food) {
        coordinator.openDeleteConfirmation(
                "Delete food?",
                "Are you sure you want to delete "
                        + food.name()
                        + "?",
                () -> deleteFood(food.id())
        );
    }

    private void deleteFood(Integer foodId) {
        AsyncTaskRunner.run(
                () -> {
                    foodService.deleteFood(foodId);
                    return null;
                },

                ignored -> {
                    foods.removeIf(
                            food -> food.id().equals(foodId)
                    );

                    FoodSearchCache.clear();

                    updateView();

                    coordinator.closeDeleteConfirmation();
                },

                exception -> {
                    coordinator.setDeleteConfirmationDeleting(false);

                    coordinator.showDeleteConfirmationError(AppConstants.Messages.DELETE_FOOD_ERROR);

                    log.error(
                            "Failed to delete food.",
                            exception
                    );
                }
        );
    }

    // ── Responsive ──────────────────────────────────────────────
    @Override
    public void updateWidthLayout(boolean narrow) {
        rootLayout.pseudoClassStateChanged(NARROW, narrow);

        HBox.setHgrow(
                addFoodButton,
                narrow
                        ? Priority.ALWAYS
                        : Priority.NEVER
        );

        addFoodButton.setMaxWidth(
                narrow
                        ? Double.MAX_VALUE
                        : Region.USE_COMPUTED_SIZE
        );
    }

    // ── Refresh ──────────────────────────────────────────────
    @Override
    public void refresh() {
        loadFoods();
    }

    // ── Button Actions ──────────────────────────────────────────────
    @FXML
    private void handleAddFood() {
        openCreateFood();
    }
}