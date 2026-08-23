package com.fittrack.controller.nutrition.components;

import com.fittrack.config.AppImages;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;

import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class DailyMealCardController implements Initializable {

    // Labels
    @FXML private Label titleLabel;
    @FXML private Label firstFoodLabel;
    @FXML private Label moreFoodsLabel;
    @FXML private Label caloriesLabel;

    // Other
    @FXML private Button menuButton;
    @FXML private ImageView mealIcon;

    // ContextMenu
    private ContextMenu contextMenu;
    private MenuItem saveMealMenuItem;

    // Button Action
    private Runnable onLogAction;
    private Runnable onOpenAction;
    private Runnable onSaveMealAction;

    // ── Configuration ─────────────────────────────────────────────────
    public void setIcon(Image icon) {
        mealIcon.setImage(icon);
    }

    public void setOnLogAction(Runnable action) {
        this.onLogAction = action;
    }

    public void setOnOpenAction(Runnable onOpenAction) {
        this.onOpenAction = onOpenAction;
    }

    public void setOnSaveMealAction(Runnable onSaveMealAction) {
        this.onSaveMealAction = onSaveMealAction;
    }

    // ── Set Data ─────────────────────────────────────────────────
    public void setData(String title, String firstFood, Integer otherFoodsCount, double calories) {
        boolean hasMealItems = firstFood != null;
        saveMealMenuItem.setDisable(!hasMealItems);

        titleLabel.setText(title);

        if (firstFood == null) {
            firstFoodLabel.setText("No foods logged");
            moreFoodsLabel.setText("");
            caloriesLabel.setText("0 cal");
        } else {
            firstFoodLabel.setText(firstFood);

            if (otherFoodsCount == null || otherFoodsCount <= 0) {
                moreFoodsLabel.setText("");
            } else {
                moreFoodsLabel.setText("and " + otherFoodsCount + " more");
            }

            caloriesLabel.setText(Math.round(calories) + " cal");
        }
    }

    // ── Initialization ─────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize ContextMenu
        initializeContextMenu();
    }

    private void initializeContextMenu() {
        ImageView bookmarkIcon = new ImageView(AppImages.BOOKMARK_ICON);
        bookmarkIcon.setFitWidth(16);
        bookmarkIcon.setFitHeight(16);

        saveMealMenuItem = new MenuItem(
                "Save as My Meal",
                bookmarkIcon
        );

        saveMealMenuItem.setOnAction(event -> {
            if (onSaveMealAction != null) {
                onSaveMealAction.run();
            }
        });

        contextMenu = new ContextMenu(saveMealMenuItem);
        contextMenu.getStyleClass().add("meal-card-context-menu");
    }

    // ── Button Actions ─────────────────────────────────────────────────
    @FXML
    private void handleLog() {
        if (onLogAction != null) {
            onLogAction.run();
        }
    }

    @FXML
    private void handleOpen(MouseEvent event) {
        if (isClickOnButton(event)) {
            return;
        }

        if (onOpenAction != null) {
            onOpenAction.run();
        }
    }

    @FXML
    private void handleMenu() {
        contextMenu.show(
                menuButton,
                Side.BOTTOM,
                0,
                0
        );
    }

    // ── Button Helpers ─────────────────────────────────────────────────
    private boolean isClickOnButton(MouseEvent event) {
        Node node = (Node) event.getTarget();

        while (node != null) {
            if (node instanceof Button) {
                return true;
            }

            node = node.getParent();
        }

        return false;
    }
}