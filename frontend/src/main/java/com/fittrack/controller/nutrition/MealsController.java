package com.fittrack.controller.nutrition;

import com.fittrack.async.AsyncTaskRunner;
import com.fittrack.config.AppConstants;
import com.fittrack.config.AppImages;
import com.fittrack.controller.common.NavigableController;
import com.fittrack.controller.common.Refreshable;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.controller.nutrition.components.DailyMealCardController;
import com.fittrack.controller.nutrition.components.NutritionProgressCardController;
import com.fittrack.coordinator.nutrition.MealsCoordinator;
import com.fittrack.dto.nutrition.meal.CopyMealRequest;
import com.fittrack.dto.nutrition.meal.MealResponse;
import com.fittrack.model.nutrition.DailyNutritionTotals;
import com.fittrack.model.nutrition.MealType;
import com.fittrack.model.nutrition.NutritionTargets;
import com.fittrack.service.nutrition.MealService;
import com.fittrack.service.nutrition.NutritionCalculationService;
import com.fittrack.service.nutrition.NutritionGoalService;
import com.fittrack.ui.FxmlComponentLoader;
import com.fittrack.ui.LoadedComponent;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
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
import java.util.*;

public class MealsController extends NavigableController implements Initializable, ResponsiveLayout, Refreshable {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(MealsController.class);

    // Root
    @FXML private StackPane rootLayout;
    @FXML private ScrollPane mealsScroll;
    @FXML private BorderPane mealsContent;

    // Wide and Narrow
    @FXML private HBox wideContainer;
    @FXML private VBox narrowContainer;

    // Top - DatePicker
    @FXML private DatePicker datePicker;

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
    private LoadedComponent<DailyMealCardController> breakfastCard;
    private LoadedComponent<DailyMealCardController> lunchCard;
    private LoadedComponent<DailyMealCardController> dinnerCard;
    private LoadedComponent<DailyMealCardController> snacksCard;

    // ScrollPane position (prevents resetting to top)
    private double overlayScrollPosition;

    // Load Helpers
    private long mealsLoadVersion;

    // Meal Copy
    private long mealAvailabilityCheckVersion;

    // Narrow Helpers
    private static final int NARROW_BREAKPOINT = 690;
    private Boolean narrowLayout;
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");

    // Constants
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.uuuu");

    // Cache
    private final Map<LocalDate, List<MealResponse>> mealsCache = new HashMap<>();

    // Coordinator
    private final MealsCoordinator coordinator = new MealsCoordinator();

    // Nutrition Targets for Progress Cards
    private NutritionTargets nutritionTargets;

    // Service
    private final MealService mealService = new MealService();
    private final NutritionGoalService nutritionGoalService = new NutritionGoalService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Setup ScrollPane
        mealsContent.minHeightProperty().bind(
                mealsScroll.viewportBoundsProperty().map(Bounds::getHeight)
        );

        // ScrollPane Position Listener
        coordinator.setOverlayLifecycle(
                () -> overlayScrollPosition = mealsScroll.getVvalue(),

                () -> Platform.runLater(() -> {
                            mealsScroll.setVvalue(overlayScrollPosition);
                            mealsScroll.requestFocus();
                        }
                )
        );

        // Initialize Summary and Diary Cards
        initializeSummaryCards();
        initializeMealCards();

        // Initialize Responsive
        initializeResponsiveWidthLayout(rootLayout, NARROW_BREAKPOINT);

        // Initialize Controls
        initializeMealsControls();

        // Load Targets
        datePicker.setValue(LocalDate.now());
    }

    // ── Refresh Actions ─────────────────────────────────────────────────
    @Override
    public void refresh() {
        LocalDate currentDate = datePicker.getValue();

        if (currentDate == null) {
            return;
        }

        invalidateMealsCache(currentDate);
        reloadMealsForDate(currentDate);
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
    private void loadMealsForDate(LocalDate date, long loadVersion) {
        List<MealResponse> cachedMeals = mealsCache.get(date);

        if (cachedMeals != null) {
            if (loadVersion != mealsLoadVersion
                    || !date.equals(datePicker.getValue())) {
                return;
            }

            showMeals(cachedMeals);
            return;
        }

        AsyncTaskRunner.run(
                () -> mealService.getMealsForDate(date),

                meals -> {
                    if (loadVersion != mealsLoadVersion) {
                        return;
                    }

                    if (!date.equals(datePicker.getValue())) {
                        return;
                    }

                    mealsCache.put(date, meals);
                    showMeals(meals);
                },

                exception -> {
                    if (loadVersion != mealsLoadVersion) {
                        return;
                    }

                    log.error(
                            "Failed to load meals for date: {}",
                            date,
                            exception
                    );
                }
        );
    }

    private void reloadMealsForDate(LocalDate date) {
        if (date == null || !date.equals(datePicker.getValue())) {
            return;
        }

        long loadVersion = ++mealsLoadVersion;
        loadMealsForDate(date, loadVersion);
    }

    private void loadNutritionTargets(LocalDate date) {
        long loadVersion = ++mealsLoadVersion;

        AsyncTaskRunner.run(
                () -> nutritionGoalService.getNutritionTargetsForDate(date),

                targets -> {
                    if (loadVersion != mealsLoadVersion) {
                        return;
                    }

                    nutritionTargets = targets;
                    loadMealsForDate(date, loadVersion);
                },

                exception -> {
                    if (loadVersion != mealsLoadVersion) {
                        return;
                    }

                    log.error(
                            "Failed to load nutrition targets for date: {}",
                            date,
                            exception
                    );
                }
        );
    }

    private void showMeals(List<MealResponse> meals) {
        // Update Summary Cards
        DailyNutritionTotals totals = NutritionCalculationService.calculateDailyNutritionTotals(meals);
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
                loadNutritionTargets(newDate);
            }
        });
    }

    // ── Summary Helpers ─────────────────────────────────────────────────
    private void updateSummary(DailyNutritionTotals totals) {
        caloriesCard.setData(
                "Calories",
                totals.calories(),
                nutritionTargets.calories(),
                "cal",
                true
        );

        wideCarbsCard.setData(
                "Carbs",
                totals.carbs(),
                nutritionTargets.carbs(),
                "g",
                false
        );

        wideFatCard.setData(
                "Fat",
                totals.fat(),
                nutritionTargets.fat(),
                "g",
                false
        );

        wideProteinCard.setData(
                "Protein",
                totals.protein(),
                nutritionTargets.protein(),
                "g",
                false
        );

        narrowCarbsCard.setData(
                "Carbs",
                totals.carbs(),
                nutritionTargets.carbs(),
                "g",
                false
        );

        narrowFatCard.setData(
                "Fat",
                totals.fat(),
                nutritionTargets.fat(),
                "g",
                false
        );

        narrowProteinCard.setData(
                "Protein",
                totals.protein(),
                nutritionTargets.protein(),
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
        breakfastCard = createMealCard("Breakfast", AppImages.BREAKFAST_ICON);
        lunchCard = createMealCard("Lunch", AppImages.LUNCH_ICON);
        dinnerCard = createMealCard("Dinner", AppImages.DINNER_ICON);
        snacksCard = createMealCard("Snacks", AppImages.SNACKS_ICON);

        diaryContent.getChildren().addAll(
                breakfastCard.root(),
                lunchCard.root(),
                dinnerCard.root(),
                snacksCard.root()
        );
    }

    private LoadedComponent<DailyMealCardController> createMealCard(String title, Image icon) {
        LoadedComponent<DailyMealCardController> component = FxmlComponentLoader.load(AppConstants.Components.DAILY_MEAL_CARD);

        component.controller().setData(title, null, 0, 0);
        component.controller().setIcon(icon);

        component.controller().setOnLogAction(() -> {
            LocalDate selectedDate = datePicker.getValue();

            coordinator.openAddToMeal(
                    MealType.fromName(title),
                    selectedDate,
                    () -> {
                        invalidateMealsCache(selectedDate);
                        reloadMealsForDate(selectedDate);
                    }
            );
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

    private void loadMealCard(String mealName, MealResponse meal, LoadedComponent<DailyMealCardController> card) {
        LocalDate selectedDate = datePicker.getValue();
        MealType mealType = MealType.fromName(mealName);

        card.controller().setOnOpenAction(() -> {
            coordinator.openMealDetails(
                    mealType,
                    selectedDate,
                    meal,
                    () -> {
                        invalidateMealsCache(selectedDate);
                        reloadMealsForDate(selectedDate);
                    }
            );
        });

        card.controller().setOnCopyFromAction(
                () -> openCopyFrom(mealType, selectedDate)
        );

        card.controller().setOnCopyToAction(
                () -> openCopyTo(mealType, selectedDate)
        );

        if (meal == null || meal.items().isEmpty()) {
            card.controller().setOnSaveMealAction(null);

            card.controller().setData(
                    mealName,
                    null,
                    0,
                    0
            );

            return;
        }

        card.controller().setOnSaveMealAction(
                () -> coordinator.openSaveAsMeal(meal)
        );

        String firstFoodName = meal.items().getFirst().foodName();
        int otherFoodsCount = meal.items().size() - 1;
        int calories = NutritionCalculationService.calculateMealCalories(meal);

        card.controller().setData(
                mealName,
                firstFoodName,
                otherFoodsCount,
                calories
        );
    }

    // ── ContextMenu Items Actions ─────────────────────────────────────────────────
    private void openCopyFrom(MealType currentMealType, LocalDate currentDate) {
        coordinator.openCopyFrom(
                currentMealType,
                currentDate,

                this::checkMealAvailability,

                (sourceMealType, sourceDate) -> {
                    CopyMealRequest request =
                            new CopyMealRequest(
                                    sourceDate,
                                    sourceMealType.getName(),
                                    currentDate,
                                    currentMealType.getName()
                            );

                    copyMeal(
                            request,
                            currentDate
                    );
                },

                () -> mealAvailabilityCheckVersion++
        );
    }

    private void openCopyTo(MealType currentMealType, LocalDate currentDate) {
        coordinator.openCopyTo(
                currentMealType,
                currentDate,

                (targetMealType, targetDate) -> {
                    CopyMealRequest request =
                            new CopyMealRequest(
                                    currentDate,
                                    currentMealType.getName(),
                                    targetDate,
                                    targetMealType.getName()
                            );

                    copyMeal(
                            request,
                            targetDate
                    );
                }
        );
    }

    private void checkMealAvailability(MealType mealType, LocalDate date) {
        long checkVersion = ++mealAvailabilityCheckVersion;

        AsyncTaskRunner.run(
                () -> mealService.hasDailyMealItems(date, mealType.getName()),

                hasItems -> {
                    if (checkVersion != mealAvailabilityCheckVersion) {
                        return;
                    }

                    coordinator.setMealAvailability(hasItems);
                },

                exception -> {
                    if (checkVersion != mealAvailabilityCheckVersion) {
                        return;
                    }

                    coordinator.setMealAvailabilityCheckFailed();

                    log.error(
                            "Failed to check meal availability.",
                            exception
                    );
                }
        );
    }

    private void copyMeal(CopyMealRequest request, LocalDate targetDate) {
        AsyncTaskRunner.run(
                () -> {
                    mealService.copyDailyMeal(request);
                    return null;
                },

                ignored -> {
                    invalidateMealsCache(targetDate);

                    coordinator.closeCopyDialog();

                    if (targetDate.equals(datePicker.getValue())) {
                        reloadMealsForDate(targetDate);
                    }
                },

                exception -> {
                    coordinator.setCopying(false);

                    log.error(
                            "Failed to copy meal.",
                            exception
                    );
                }
        );
    }

    // ── Cache Helpers ─────────────────────────────────────────────────
    private void invalidateMealsCache(LocalDate date) {
        mealsCache.remove(date);
    }
}