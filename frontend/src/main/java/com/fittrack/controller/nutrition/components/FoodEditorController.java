package com.fittrack.controller.nutrition.components;

import com.fittrack.config.AppConstants;
import com.fittrack.controller.common.FormController;
import com.fittrack.dto.nutrition.food.CreateFoodRequest;
import com.fittrack.ui.SceneShortcuts;
import com.fittrack.util.NumberUtils;
import com.fittrack.validation.FitnessInputValidator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class FoodEditorController extends FormController implements Initializable {

    // Custom console messages
    private static final Logger log = LoggerFactory.getLogger(FoodEditorController.class);

    @Override
    protected Logger getLogger() { return log; }

    @FXML private VBox rootLayout;
    @FXML private ScrollPane setupScroll;
    @FXML private VBox editorContent;

    @FXML private Label titleLabel;

    @FXML private TextField nameField;
    @FXML private Label nameMessage;

    @FXML private TextField brandField;
    @FXML private Label brandMessage;

    @FXML private TextField servingSizeField;
    @FXML private Label servingSizeMessage;

    @FXML private TextField caloriesField;
    @FXML private Label caloriesMessage;

    @FXML private TextField carbsField;
    @FXML private Label carbsMessage;

    @FXML private TextField fatField;
    @FXML private Label fatMessage;

    @FXML private TextField proteinField;
    @FXML private Label proteinMessage;

    @FXML private Button saveButton;
    @FXML private Button deleteFoodButton;

    // Actions
    private Runnable onCancelAction;
    private Consumer<CreateFoodRequest> onCreateAction;

    // ── Configuration ──────────────────────────────────────────
    public void setOnCancelAction(Runnable onCancelAction) {
        this.onCancelAction = onCancelAction;
    }

    public void setOnCreateAction(Consumer<CreateFoodRequest> onCreateAction) {
        this.onCreateAction = onCreateAction;
    }

    public void setCreateMode() {
        titleLabel.setText("Create Food");
        saveButton.setText("Create food");

        setVisible(deleteFoodButton, false);

        clearFields();
        restoreHelpers();
    }

    // ── Initialization ─────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Setup ScrollPane
        editorContent.minHeightProperty().bind(
                setupScroll.viewportBoundsProperty().map(Bounds::getHeight)
        );

        // Keyboard shortcuts
        SceneShortcuts.forNode(rootLayout)
                .onEscape(this::handleCancel)
                .onEnter(this::handleSave);

        // Add Listeners
        addListeners();
        restoreHelpers();

        // Default Mode is CREATE
        setCreateMode();
    }

    private void addListeners() {
        nameField.textProperty().addListener((observable, oldValue, newValue) -> restoreNameHelper());
        servingSizeField.textProperty().addListener((observable, oldValue, newValue) -> restoreServingSizeHelper());
        caloriesField.textProperty().addListener((observable, oldValue, newValue) -> restoreCaloriesHelper());
        carbsField.textProperty().addListener((observable, oldValue, newValue) -> restoreCarbsHelper());
        fatField.textProperty().addListener((observable, oldValue, newValue) -> restoreFatHelper());
        proteinField.textProperty().addListener((observable, oldValue, newValue) -> restoreProteinHelper());
    }

    // ── Button Actions ─────────────────────────────────────────────────
    @FXML
    private void handleCancel() {
        if (onCancelAction != null) {
            onCancelAction.run();
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        if (!validateNutritionRelations()) {
            return;
        }

        restoreHelpers();

        if (onCreateAction != null) {
            CreateFoodRequest request = createFoodRequest();
            onCreateAction.accept(request);
        }
    }

    @FXML
    private void handleDeleteFood() {
        // TODO: Edit mode comes later.
    }

    // ── Request ────────────────────────────────────────────────
    private CreateFoodRequest createFoodRequest() {
        String name = nameField.getText().trim();
        String brand = brandField.getText().trim().isEmpty() ? null : brandField.getText().trim();
        double servingSize = NumberUtils.parseDecimal(servingSizeField.getText().trim());
        double calories = NumberUtils.parseDecimal(caloriesField.getText().trim());
        double carbs = NumberUtils.parseDecimal(carbsField.getText().trim());
        double fat = NumberUtils.parseDecimal(fatField.getText().trim());
        double protein = NumberUtils.parseDecimal(proteinField.getText().trim());

        return new CreateFoodRequest(
            name,
            brand,
            servingSize,
            calories,
            carbs,
            fat,
            protein
        );
    }

    // ── Field State ────────────────────────────────────────────
    private void clearFields() {
        nameField.clear();
        brandField.clear();
        servingSizeField.clear();
        caloriesField.clear();
        carbsField.clear();
        fatField.clear();
        proteinField.clear();
    }

    // ── Validation ─────────────────────────────────────────────────
    private boolean validateInputs() {
        String name = nameField.getText().trim();
        String servingSize = servingSizeField.getText().trim();
        String calories = caloriesField.getText().trim();
        String carbs = carbsField.getText().trim();
        String fat = fatField.getText().trim();
        String protein = proteinField.getText().trim();

        boolean valid = true;

        if (name.isBlank()) {
            showNameMessage();
            shake(nameField);
            valid = false;
        }

        if (!FitnessInputValidator.isPositiveFoodDecimal(servingSize)) {
            showServingSizeMessage();
            shake(servingSizeField);
            valid = false;
        }

        if (!FitnessInputValidator.isNonNegativeFoodDecimal(calories)) {
            showCaloriesMessage();
            shake(caloriesField);
            valid = false;
        }

        if (!FitnessInputValidator.isNonNegativeFoodDecimal(carbs)) {
            showCarbsMessage();
            shake(carbsField);
            valid = false;
        }

        if (!FitnessInputValidator.isNonNegativeFoodDecimal(fat)) {
            showFatMessage();
            shake(fatField);
            valid = false;
        }

        if (!FitnessInputValidator.isNonNegativeFoodDecimal(protein)) {
            showProteinMessage();
            shake(proteinField);
            valid = false;
        }

        return valid;
    }

    private boolean validateNutritionRelations() {
        double servingSize = NumberUtils.parseDecimal(servingSizeField.getText().trim());
        double calories = NumberUtils.parseDecimal(caloriesField.getText().trim());
        double carbs = NumberUtils.parseDecimal(carbsField.getText().trim());
        double fat = NumberUtils.parseDecimal(fatField.getText().trim());
        double protein = NumberUtils.parseDecimal(proteinField.getText().trim());

        if (!FitnessInputValidator.areMacrosWithinServingSize(servingSize, carbs, fat, protein)) {
            showMacrosExceedingServingSizeMessage();
            shake(servingSizeField);
            shake(carbsField);
            shake(fatField);
            shake(proteinField);
            return false;
        }

        if (!FitnessInputValidator.areCaloriesReasonable(calories, carbs, fat, protein)) {
            showCaloriesMismatchMessage();
            shake(caloriesField);
            shake(carbsField);
            shake(fatField);
            shake(proteinField);
            return false;
        }

        return true;
    }

    // ── Helper State ───────────────────────────────────────────
    private void restoreHelpers() {
        restoreNameHelper();
        setBrandMessage();
        restoreServingSizeHelper();
        restoreCaloriesHelper();
        restoreCarbsHelper();
        restoreFatHelper();
        restoreProteinHelper();
    }

    // ── Submit State ─────────────────────────────────────────────
    public void setSubmitting(boolean submitting) {
        saveButton.setDisable(submitting);
    }

    // ── Name Helpers ─────────────────────────────────────────────────
    private void showNameMessage() {
        setFieldMessage(nameMessage, AppConstants.Messages.INVALID_FOOD_NAME_MESSAGE, true, nameField);
    }

    private void restoreNameHelper() {
        setFieldMessage(nameMessage, AppConstants.Messages.HELPER_FOOD_NAME_MESSAGE, false, nameField);
    }

    // ── Brand Helpers ─────────────────────────────────────────────────
    private void setBrandMessage() {
        setFieldMessage(brandMessage, AppConstants.Messages.HELPER_BRAND_MESSAGE, false, brandField);
    }

    // ── Serving Size Helpers ─────────────────────────────────────────────────
    private void showServingSizeMessage() {
        setFieldMessage(servingSizeMessage, AppConstants.Messages.INVALID_FOOD_SERVING_SIZE_MESSAGE, true, servingSizeField);
    }

    private void restoreServingSizeHelper() {
        setFieldMessage(servingSizeMessage, AppConstants.Messages.HELPER_FOOD_SERVING_SIZE_MESSAGE, false, servingSizeField);
    }

    // ── Calories Helpers ─────────────────────────────────────────────────
    private void showCaloriesMessage() {
        setFieldMessage(caloriesMessage, AppConstants.Messages.INVALID_FOOD_CALORIES_MESSAGE, true, caloriesField);
    }

    private void restoreCaloriesHelper() {
        setFieldMessage(caloriesMessage, AppConstants.Messages.HELPER_FOOD_CALORIES_MESSAGE, false, caloriesField);
    }

    // ── Carbs Helpers ─────────────────────────────────────────────────
    private void showCarbsMessage() {
        setFieldMessage(carbsMessage, AppConstants.Messages.INVALID_FOOD_CARBS_MESSAGE, true, carbsField);
    }

    private void restoreCarbsHelper() {
        setFieldMessage(carbsMessage, AppConstants.Messages.HELPER_FOOD_CARBS_MESSAGE, false, carbsField);
    }

    // ── Fat Helpers ─────────────────────────────────────────────────
    private void showFatMessage() {
        setFieldMessage(fatMessage, AppConstants.Messages.INVALID_FOOD_FAT_MESSAGE, true, fatField);
    }

    private void restoreFatHelper() {
        setFieldMessage(fatMessage, AppConstants.Messages.HELPER_FOOD_FAT_MESSAGE, false, fatField);
    }

    // ── Protein Helpers ─────────────────────────────────────────────────
    private void showProteinMessage() {
        setFieldMessage(proteinMessage, AppConstants.Messages.INVALID_FOOD_PROTEIN_MESSAGE, true, proteinField);
    }

    private void restoreProteinHelper() {
        setFieldMessage(proteinMessage, AppConstants.Messages.HELPER_FOOD_PROTEIN_MESSAGE, false, proteinField);
    }

    // ── Specific Error Helpers ─────────────────────────────────────────────────
    private void showMacrosExceedingServingSizeMessage() {
        setFieldMessage(servingSizeMessage, AppConstants.Messages.INVALID_MACROS_EXCEED_SERVING_MESSAGE, true, servingSizeField, carbsField, fatField, proteinField);
    }

    private void showCaloriesMismatchMessage() {
        setFieldMessage(caloriesMessage, AppConstants.Messages.INVALID_CALORIES_MISMATCH_MESSAGE, true, caloriesField, carbsField, fatField, proteinField);
    }
}
