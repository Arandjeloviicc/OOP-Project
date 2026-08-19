package com.fittrack.controller.nutrition;

import com.fittrack.api.nutrition.FoodApi;
import com.fittrack.api.nutrition.MealApi;
import com.fittrack.controller.common.FormController;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.controller.nutrition.components.FoodListItemController;
import com.fittrack.controller.nutrition.components.NutritionMacroPreviewController;
import com.fittrack.dto.nutrition.AddMealItemRequest;
import com.fittrack.dto.nutrition.FoodResponse;
import com.fittrack.model.nutrition.MealType;
import com.fittrack.session.UserSession;
import com.fittrack.util.*;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AddFoodController extends FormController implements Initializable, ResponsiveLayout {

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
    @FXML private ComboBox<MealType> mealComboBox;
    @FXML private HBox servingRow;
    @FXML private VBox servingSizeContainer;
    @FXML private VBox servingsContainer;
    @FXML private ComboBox<String> servingSizeComboBox;
    @FXML private TextField servingsField;
    @FXML private Label servingsMessage;
    @FXML private VBox nutrientPreview;

    // Attributes
    private MealType mealType;
    private LocalDate mealDate;
    private Runnable onCloseAction;
    private boolean dataChanged;

    private FoodResponse selectedFood;
    private NutritionMacroPreviewController nutritionMacroPreviewController;

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

        initializeResponsiveWidthLayout(rootLayout, NARROW_BREAKPOINT);
        initializeResponsiveHeightLayout(rootLayout, SHORT_BREAKPOINT);

        initializeServingLayout();

        mealComboBox.getItems().setAll(MealType.values());

        servingsField.setText("1");

        TextFieldValidators.applyDecimalFilter(servingsField);
        servingsField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateNutrientsInfo();
            restoreServingsHelper();
        });

        servingSizeComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> updateNutrientsInfo()
        );

        restoreServingsHelper();
    }

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
        nutritionMacroPreviewController = null;

        servingsField.clear();
        servingsField.setText("1");
        restoreServingsHelper();

        servingSizeComboBox.getItems().clear();

        setVisible(foodDetailsContainer, false);
        setVisible(foodListContainer, true);
    }

    @FXML
    private void handleConfirmAddFood() {
        String servingsText = servingsField.getText();

        if (servingsText.isEmpty()) {
            showServingsMessage();
            shake(servingsField);
            return;
        }

        double numberOfServings;

        try {
            numberOfServings = NumberUtils.parseDecimal(servingsText);
        } catch (NumberFormatException _) {
            showServingsMessage();
            shake(servingsField);
            return;
        }

        if (numberOfServings <= 0) {
            showServingsMessage();
            shake(servingsField);
            return;
        }

        double servingSizeGrams = getSelectedServingSizeGrams();

        double quantityGrams = servingSizeGrams * numberOfServings;

        addFoodToMeal(quantityGrams);
    }

    private void initializeServingLayout() {
        servingSizeContainer.prefWidthProperty().bind(
                servingRow.widthProperty()
                        .subtract(servingRow.getSpacing())
                        .multiply(0.4)
        );

        servingsContainer.prefWidthProperty().bind(
                servingRow.widthProperty()
                        .subtract(servingRow.getSpacing())
                        .multiply(0.6)
        );
    }

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
        mealComboBox.setValue(mealType);
        servingSizeComboBox.getItems().setAll(
                Math.round(food.servingSizeGrams()) + " g"
        );

        servingSizeComboBox.getSelectionModel().selectFirst();

        LoadedComponent<NutritionMacroPreviewController> nutrients = FxmlComponentLoader.load(AppConstants.Components.NUTRITION_MACRO_PREVIEW);
        nutritionMacroPreviewController = nutrients.controller();
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

    private void addFoodToMeal(double quantityGrams) {
        Integer userId = UserSession.getInstance().getCurrentUser().id();

        MealType selectedMeal = mealComboBox.getValue();

        AddMealItemRequest request = new AddMealItemRequest(mealDate, selectedMeal.getName(), selectedFood.id(), quantityGrams);

        AsyncTaskRunner.run(
                () -> mealApi.addMealItem(userId, request),

                response -> {
                    dataChanged = true;
                    handleCancelFoodDetails();
                },

                exception -> log.error(
                        "Failed to add food to meal.",
                        exception
                )
        );
    }

    private void updateNutrientsInfo() {
        if (selectedFood == null
                || nutritionMacroPreviewController == null
                || servingSizeComboBox.getValue() == null) {
            return;
        }

        double numberOfServings;
        try {
            numberOfServings = NumberUtils.parseDecimal(servingsField.getText());
        } catch (NumberFormatException _) {
            return;
        }

        if (numberOfServings <= 0) {
            return;
        }

        double selectedServingSize = getSelectedServingSizeGrams();
        double baseServingSize = selectedFood.servingSizeGrams();

        if (baseServingSize <= 0) {
            return;
        }

        double quantityGrams =
                selectedServingSize * numberOfServings;

        double ratio = quantityGrams / baseServingSize;

        double calories = selectedFood.caloriesPerServing() * ratio;
        double carbs = selectedFood.carbsPerServing() * ratio;
        double fat = selectedFood.fatPerServing() * ratio;
        double protein = selectedFood.proteinPerServing() * ratio;

        nutritionMacroPreviewController.setData(calories, carbs, fat, protein);

    }

    private double getSelectedServingSizeGrams() {
        return selectedFood.servingSizeGrams();
    }

    // ── Quantity Helpers ─────────────────────────────────────────────────
    private void showServingsMessage() {
        setFieldMessage(servingsMessage, AppConstants.Messages.INVALID_SERVINGS_MESSAGE, true, servingsField);
    }

    private void restoreServingsHelper() {
        setFieldMessage(servingsMessage, "", false, servingsField);
    }
}