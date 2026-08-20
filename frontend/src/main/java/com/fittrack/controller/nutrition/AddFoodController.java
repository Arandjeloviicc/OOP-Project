package com.fittrack.controller.nutrition;

import com.fittrack.api.nutrition.FoodApi;
import com.fittrack.api.nutrition.MealApi;
import com.fittrack.async.AsyncTaskRunner;
import com.fittrack.cache.FoodSearchCache;
import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.FormController;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.controller.nutrition.components.FoodListItemController;
import com.fittrack.controller.nutrition.components.MealItemDetailsController;
import com.fittrack.dto.nutrition.AddMealItemRequest;
import com.fittrack.dto.nutrition.FoodResponse;
import com.fittrack.model.nutrition.MealType;
import com.fittrack.session.UserSession;
import com.fittrack.ui.FxmlComponentLoader;
import com.fittrack.ui.LoadedComponent;
import com.fittrack.ui.OverlayManager;
import javafx.animation.PauseTransition;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

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
    @FXML private VBox itemsContainer;

    // Details
    @FXML private VBox foodDetailsContainer;

    // Attributes
    private MealType mealType;
    private LocalDate mealDate;
    private Runnable onCloseAction;
    private boolean dataChanged;

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

        // Search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchDebounce.stop();

            searchDebounce.setOnFinished(event -> loadAllFoods(newValue));
            searchDebounce.playFromStart();
        });
    }

    // ── Button Actions ─────────────────────────────────────────────────
    @FXML
    private void handleClose() {
        OverlayManager.close();

        if (dataChanged && onCloseAction != null) {
            onCloseAction.run();
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

    // ── Context & Callbacks ────────────────────────────────────────────
    public void setContext(MealType mealType, LocalDate mealDate) {
        this.mealType = mealType;
        this.mealDate = mealDate;
        this.dataChanged = false;

        titleLabel.setText("Add to " + mealType.getName());

        loadAllFoods("");
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    // ── Food Loading ───────────────────────────────────────────────────
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

    // ── Food Details ───────────────────────────────────────────────────
    private void openFoodDetails(FoodResponse food) {
        LoadedComponent<MealItemDetailsController> details = FxmlComponentLoader.load(AppConstants.Components.MEAL_ITEM_DETAILS);

        details.controller().setData(food, mealType);
        details.controller().setCaption("Add food");
        details.controller().setConfirmButtonText("Add to meal");

        details.controller().setOnCancelAction(
                this::closeFoodDetails
        );

        details.controller().setOnConfirmAction(
                quantityGrams -> addFoodToMeal(
                        food,
                        details.controller().getSelectedMealType(),
                        quantityGrams
                )
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

    // ── Cache Helpers ──────────────────────────────────────────────
    private String normalizeSearch(String search) {
        return search == null
                ? ""
                : search.trim().toLowerCase(Locale.ROOT);
    }

}