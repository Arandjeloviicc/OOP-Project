package com.fittrack.controller.nutrition;

import com.fittrack.api.nutrition.FoodApi;
import com.fittrack.api.nutrition.MealApi;
import com.fittrack.controller.common.BaseController;
import com.fittrack.controller.nutrition.components.FoodListItemController;
import com.fittrack.controller.nutrition.components.NutritionMacroPreviewController;
import com.fittrack.dto.nutrition.AddMealItemRequest;
import com.fittrack.dto.nutrition.FoodResponse;
import com.fittrack.session.UserSession;
import com.fittrack.util.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AddFoodController extends BaseController implements Initializable {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(AddFoodController.class);

    @Override
    protected Logger getLogger() { return log; }

    // Root
    @FXML private StackPane rootLayout;
    @FXML private VBox dialogContainer;

    // Food List Container
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
    @FXML private Label selectedFoodTitleLabel;
    @FXML private ComboBox<String> mealComboBox;
    @FXML private TextField quantityField;
    @FXML private ComboBox<String> unitComboBox;
    @FXML private Label defaultServingLabel;
    @FXML private VBox nutrientPreview;

    // Attributes
    private String mealName;
    private LocalDate mealDate;
    private Runnable onCloseAction;
    private boolean dataChanged;

    private FoodResponse selectedFood;

    // Api
    private final FoodApi foodApi = new FoodApi();
    private final MealApi mealApi = new MealApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mealComboBox.getItems().setAll(
                "Breakfast",
                "Lunch",
                "Dinner",
                "Snacks"
        );

        unitComboBox.getItems().setAll("g");
        unitComboBox.setValue("g");
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
    private void handleCancelFoodDetails() {
        selectedFood = null;

        setVisible(foodDetailsContainer, false);
        setVisible(foodListContainer, true);
    }

    @FXML
    private void handleConfirmAddFood() {
        log.info("Confirm Add Food");
    }

    public void setContext(String mealName, LocalDate mealDate) {
        this.mealName = mealName;
        this.mealDate = mealDate;
        this.dataChanged = false;

        titleLabel.setText("Add to " + mealName);

        loadAllFoods("");
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    private void loadAllFoods(String search) {
        AsyncTaskRunner.run(
                () -> foodApi.searchFoods(search),

                foods -> {
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

    private void openFoodDetails(FoodResponse food) {
        selectedFood = food;

        selectedFoodTitleLabel.setText(food.name());
        mealComboBox.setValue(mealName);
        quantityField.setText(String.valueOf(Math.round(food.servingSizeGrams())));
        defaultServingLabel.setText(
                "Default serving: "
                        + Math.round(food.servingSizeGrams())
                        + " g"
        );

        LoadedComponent<NutritionMacroPreviewController> nutrients = FxmlComponentLoader.load(AppConstants.Components.NUTRITION_MACRO_PREVIEW);
        nutrients.controller().setData(
                food.caloriesPerServing(),
                food.carbsPerServing(),
                food.fatPerServing(),
                food.proteinPerServing()
        );

        nutrientPreview.getChildren().setAll(nutrients.root());

        setVisible(foodListContainer, false);
        setVisible(foodDetailsContainer, true);
    }

    private void addFoodToMeal(FoodResponse food) {
        Integer userId = UserSession.getInstance().getCurrentUser().id();

        AddMealItemRequest request = new AddMealItemRequest(mealDate, mealName, food.id(), food.servingSizeGrams());

        AsyncTaskRunner.run(
                () -> mealApi.addMealItem(userId, request),

                response -> {
                    dataChanged = true;
                },

                exception -> log.error(
                        "Failed to add food to meal.",
                        exception
                )
        );
    }
}