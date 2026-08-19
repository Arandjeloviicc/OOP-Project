package com.fittrack.controller.nutrition;

import com.fittrack.api.nutrition.MealApi;
import com.fittrack.controller.common.BaseController;
import com.fittrack.controller.common.Refreshable;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.controller.nutrition.components.MealCardController;
import com.fittrack.controller.nutrition.components.NutritionProgressCardController;
import com.fittrack.dto.nutrition.MealResponse;
import com.fittrack.model.nutrition.DailyNutritionTotals;
import com.fittrack.model.nutrition.MealType;
import com.fittrack.service.nutrition.MealService;
import com.fittrack.session.UserSession;
import com.fittrack.util.*;
import javafx.beans.binding.DoubleBinding;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class MealsController extends BaseController implements Initializable, ResponsiveLayout, Refreshable {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(MealsController.class);

    @Override
    protected Logger getLogger() { return log; }

    // Root
    @FXML private StackPane rootLayout;
    @FXML private ScrollPane mealsScroll;
    @FXML private BorderPane mealsContent;

    // Wide and Narrow
    @FXML private StackPane bodyStack;
    @FXML private HBox wideContainer;
    @FXML private VBox narrowContainer;

    // Top - DatePicker
    @FXML private HBox dateNavigation;
    @FXML private Button previousDayButton;
    @FXML private DatePicker datePicker;
    @FXML private Button nextDayButton;

    // Center - Summary + Diary
    // Summary
    @FXML private VBox summaryContent;
    @FXML private Label summaryTitle;
    @FXML private VBox caloriesContainer;
    @FXML private VBox wideMacrosContainer;
    @FXML private HBox narrowMacrosContainer;

    // Summary cards
    private NutritionProgressCardController caloriesCard;

    private NutritionProgressCardController wideCarbsCard;
    private NutritionProgressCardController wideFatCard;
    private NutritionProgressCardController wideProteinCard;

    private NutritionProgressCardController narrowCarbsCard;
    private NutritionProgressCardController narrowFatCard;
    private NutritionProgressCardController narrowProteinCard;

    // Diary
    @FXML private VBox diaryContent;
    private LoadedComponent<MealCardController> breakfastCard;
    private LoadedComponent<MealCardController> lunchCard;
    private LoadedComponent<MealCardController> dinnerCard;
    private LoadedComponent<MealCardController> snacksCard;

    // Narrow Helpers
    private static final int NARROW_BREAKPOINT = 690;
    private Boolean narrowLayout;
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");

    // Constants
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.uuuu");

    // Api
    private final MealApi mealApi = new MealApi();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Setup ScrollPane
        mealsContent.minHeightProperty().bind(
                mealsScroll.viewportBoundsProperty().map(Bounds::getHeight)
        );

        // Initialize Summary and Diary Cards
        initializeSummaryCards();
        initializeMealCards();

        // Initialize Responsive
        initializeResponsiveWidthLayout(rootLayout, NARROW_BREAKPOINT);

        // Initialize Controls
        initializeMealsControls();
    }

    @Override
    public void refresh() {
        loadMealsForDate();
    }

    // ── Button Actions ─────────────────────────────────────────────────
    @FXML
    public void handlePreviousDay() {
        LocalDate currentValue = datePicker.getValue();
        if (currentValue != null) {
            datePicker.setValue(currentValue.minusDays(1));
        } else {
            datePicker.setValue(LocalDate.now().minusDays(1));
        }
    }

    @FXML
    public void handleNextDay() {
        LocalDate currentValue = datePicker.getValue();
        if (currentValue != null) {
            datePicker.setValue(currentValue.plusDays(1));
        } else {
            datePicker.setValue(LocalDate.now().plusDays(1));
        }
    }

    // ── Load Data ─────────────────────────────────────────────────
    private void loadMealsForDate() {
        LocalDate currentDate = datePicker.getValue();
        Integer userId = UserSession.getInstance().getCurrentUser().id();

        AsyncTaskRunner.run(
                () -> mealApi.getMealsFromDate(userId, currentDate),

                meals -> {

                    if (!currentDate.equals(datePicker.getValue())) {
                        return;
                    }

                    // Update Summary Cards
                    DailyNutritionTotals totals = MealService.calculateDailyNutritionTotals(meals);
                    updateSummary(totals);

                    // Update Diary Cards
                    MealResponse breakfast = findMealByName(meals, "Breakfast");
                    MealResponse lunch = findMealByName(meals, "Lunch");
                    MealResponse dinner = findMealByName(meals, "Dinner");
                    MealResponse snacks = findMealByName(meals, "Snacks");

                    loadMealCard("Breakfast", breakfast, breakfastCard);
                    loadMealCard("Lunch", lunch, lunchCard);
                    loadMealCard("Dinner", dinner, dinnerCard);
                    loadMealCard("Snacks", snacks, snacksCard);
                },

                exception -> log.error("Failed to load meals for date: {}", currentDate, exception)
        );
    }

    // ── Responsive Helpers ─────────────────────────────────────────────────
    @Override
    public void updateWidthLayout(boolean narrow) {
        if (Objects.equals(narrowLayout, narrow)) return;

        narrowLayout = narrow;

        rootLayout.pseudoClassStateChanged(NARROW, narrow);

        if (narrow) {
            wideContainer.getChildren().clear();
            narrowContainer.getChildren().setAll(summaryContent, diaryContent);

            setVisible(wideContainer, false);
            setVisible(narrowContainer, true);

        } else {
            narrowContainer.getChildren().clear();
            wideContainer.getChildren().setAll(diaryContent, summaryContent);

            setVisible(narrowContainer, false);
            setVisible(wideContainer, true);
        }

        setVisible(wideMacrosContainer, !narrowLayout);
        setVisible(narrowMacrosContainer, narrowLayout);

        setVisible(summaryTitle, !narrow);

        updatePanelWidths(narrow);
        updateDateNavigation(narrow);
    }

    private void updatePanelWidths(boolean narrow) {
        summaryContent.prefWidthProperty().unbind();
        diaryContent.prefWidthProperty().unbind();

        summaryContent.setPrefWidth(Region.USE_COMPUTED_SIZE);
        diaryContent.setPrefWidth(Region.USE_COMPUTED_SIZE);

        if (!narrow) {
            DoubleBinding availableWidth = wideContainer.widthProperty()
                    .subtract(wideContainer.spacingProperty())
                    .subtract(wideContainer.getPadding().getLeft())
                    .subtract(wideContainer.getPadding().getRight());

            diaryContent.prefWidthProperty().bind(availableWidth.multiply(0.65));
            summaryContent.prefWidthProperty().bind(availableWidth.multiply(0.35));
        }
    }

    private void updateDateNavigation(boolean narrow) {
        HBox.setHgrow(
                datePicker,
                narrow ? Priority.ALWAYS : Priority.NEVER
        );
    }

    // ── Initialize Helpers ─────────────────────────────────────────────────
    private void initializeMealsControls() {
        datePicker.setConverter(new StringConverter<>() {

            @Override
            public String toString(LocalDate date) {
                if (date == null) {
                    return "";
                }

                return date.format(DATE_FORMATTER);
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) {
                    return datePicker.getValue();
                }

                try {
                    return LocalDate.parse(
                            text.trim(),
                            DATE_FORMATTER
                    );
                } catch (DateTimeParseException _) {
                    return datePicker.getValue();
                }
            }
        });

        datePicker.valueProperty().addListener((observable, oldDate, newDate) -> {
            if (newDate != null) {
                loadMealsForDate();
            }
        });
        datePicker.setValue(LocalDate.now());
    }

    // ── Summary Helpers ─────────────────────────────────────────────────
    private void updateSummary(DailyNutritionTotals totals) {
        caloriesCard.setData(
                "Calories",
                totals.calories(),
                2200,
                "cal",
                true
        );

        wideCarbsCard.setData(
                "Carbs",
                totals.carbs(),
                250,
                "g",
                false
        );

        wideFatCard.setData(
                "Fat",
                totals.fat(),
                70,
                "g",
                false
        );

        wideProteinCard.setData(
                "Protein",
                totals.protein(),
                160,
                "g",
                false
        );

        narrowCarbsCard.setData(
                "Carbs",
                totals.carbs(),
                250,
                "g",
                false
        );

        narrowFatCard.setData(
                "Fat",
                totals.fat(),
                70,
                "g",
                false
        );

        narrowProteinCard.setData(
                "Protein",
                totals.protein(),
                160,
                "g",
                false
        );
    }

    private void initializeSummaryCards() {
        initializeCalorieCard();
        loadWideMacros();
        loadNarrowMacros();
    }

    private void initializeCalorieCard() {
        LoadedComponent<NutritionProgressCardController> calories = createNutritionCard("Calories", "cal", true);

        caloriesCard = calories.controller();

        caloriesContainer.getChildren().setAll(calories.root());
    }

    private void loadNarrowMacros() {
        LoadedComponent<NutritionProgressCardController> carbs = createNutritionCard("Carbs", "g", false);
        LoadedComponent<NutritionProgressCardController> fat = createNutritionCard("Fat", "g", false);
        LoadedComponent<NutritionProgressCardController> protein = createNutritionCard("Protein", "g", false);

        carbs.controller().setProgressStyle("progress-carbs");
        fat.controller().setProgressStyle("progress-fat");
        protein.controller().setProgressStyle("progress-protein");

        carbs.root().getStyleClass().add("narrow");
        fat.root().getStyleClass().add("narrow");
        protein.root().getStyleClass().add("narrow");

        HBox.setHgrow(carbs.root(), Priority.ALWAYS);
        HBox.setHgrow(fat.root(), Priority.ALWAYS);
        HBox.setHgrow(protein.root(), Priority.ALWAYS);

        narrowCarbsCard = carbs.controller();
        narrowFatCard = fat.controller();
        narrowProteinCard = protein.controller();

        narrowMacrosContainer.getChildren().setAll(
                carbs.root(),
                fat.root(),
                protein.root()
        );
    }

    private void loadWideMacros() {
        LoadedComponent<NutritionProgressCardController> carbs = createNutritionCard("Carbs", "g", false);
        LoadedComponent<NutritionProgressCardController> fat = createNutritionCard("Fat", "g", false);
        LoadedComponent<NutritionProgressCardController> protein = createNutritionCard("Protein", "g", false);

        carbs.controller().setProgressStyle("progress-carbs");
        fat.controller().setProgressStyle("progress-fat");
        protein.controller().setProgressStyle("progress-protein");

        wideCarbsCard = carbs.controller();
        wideFatCard = fat.controller();
        wideProteinCard = protein.controller();

        wideMacrosContainer.getChildren().setAll(
                carbs.root(),
                fat.root(),
                protein.root()
        );
    }

    private LoadedComponent<NutritionProgressCardController> createNutritionCard(String title, String unit, boolean showRemaining) {
        LoadedComponent<NutritionProgressCardController> component = FxmlComponentLoader.load(AppConstants.Components.NUTRITION_PROGRESS_CARD);

        component.controller().setData(title, 0, 0, unit, showRemaining);

        return component;
    }

    // ── Diary Helpers ─────────────────────────────────────────────────
    private void initializeMealCards() {
        breakfastCard = createMealCard("Breakfast", AppImages.USER_PROFILE_ICON);
        lunchCard = createMealCard("Lunch", AppImages.DASHBOARD_ICON);
        dinnerCard = createMealCard("Dinner", AppImages.CALCULATORS_ICON);

        snacksCard = createMealCard("Snacks", AppImages.MEASUREMENTS_ICON);

        diaryContent.getChildren().addAll(
                breakfastCard.root(),
                lunchCard.root(),
                dinnerCard.root(),
                snacksCard.root()
        );
    }

    private LoadedComponent<MealCardController> createMealCard(String title, Image icon) {
        LoadedComponent<MealCardController> component = FxmlComponentLoader.load(AppConstants.Components.MEAL_CARD);

        component.controller().setData(title, null, 0, 0);
        component.controller().setIcon(icon);

        component.controller().setOnLogAction(() -> {
            LoadedComponent<AddFoodController> addFood = FxmlComponentLoader.load(AppConstants.Views.ADD_FOOD);

            addFood.controller().setContext(MealType.fromName(title), datePicker.getValue());

            addFood.controller().setOnCloseAction(this::refresh);

            OverlayManager.show(addFood.root());
        });

        return component;
    }

    private MealResponse findMealByName(List<MealResponse> meals, String name) {
        for (MealResponse meal : meals) {
            if (meal.name().equals(name)) {
                return meal;
            }
        }

        return null;
    }

    private void loadMealCard(String mealName, MealResponse meal, LoadedComponent<MealCardController> card) {
        if (meal == null || meal.items().isEmpty()) {
            card.controller().setData(
                    mealName,
                    null,
                    0,
                    0
            );

            return;
        }

        String firstFoodName = meal.items().getFirst().foodName();
        int otherFoodsCount = meal.items().size() - 1;
        int calories = MealService.calculateMealCalories(meal);

        card.controller().setData(
                mealName,
                firstFoodName,
                otherFoodsCount,
                calories
        );
    }
}