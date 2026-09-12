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
    private MenuItem copyFromMenuItem;
    private MenuItem copyToMenuItem;

    // Button Action
    private Runnable onLogAction;
    private Runnable onOpenAction;
    private Runnable onSaveMealAction;
    private Runnable onCopyFromAction;
    private Runnable onCopyToAction;

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

    public void setOnCopyFromAction(Runnable onCopyFromAction) {
        this.onCopyFromAction = onCopyFromAction;
    }

    public void setOnCopyToAction(Runnable onCopyToAction) {
        this.onCopyToAction = onCopyToAction;
    }

    // ── Set Data ─────────────────────────────────────────────────
    public void setData(String title, String firstFood, Integer otherFoodsCount, double calories) {
        boolean hasMealItems = firstFood != null;
        setMenuItemVisible(saveMealMenuItem, hasMealItems);
        setMenuItemVisible(copyToMenuItem, hasMealItems);

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
        initializeSaveMealMenuItem();
        initializeCopyFromMenuItem();
        initializeCopyToMenuItem();

        contextMenu = new ContextMenu(saveMealMenuItem, copyFromMenuItem, copyToMenuItem);
        contextMenu.getStyleClass().add("meal-card-context-menu");
    }

    private void initializeSaveMealMenuItem() {
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
    }

    private void initializeCopyFromMenuItem() {
        ImageView copyFromIcon = new ImageView(AppImages.COPY_FROM_ICON);
        copyFromIcon.setFitWidth(16);
        copyFromIcon.setFitHeight(16);

        copyFromMenuItem = new MenuItem(
                "Copy from...",
                copyFromIcon
        );

        copyFromMenuItem.setOnAction(event -> {
            if (onCopyFromAction != null) {
                onCopyFromAction.run();
            }
        });
    }

    private void initializeCopyToMenuItem() {
        ImageView copyToIcon = new ImageView(AppImages.COPY_TO_ICON);
        copyToIcon.setFitWidth(16);
        copyToIcon.setFitHeight(16);

        copyToMenuItem = new MenuItem(
                "Copy to...",
                copyToIcon
        );

        copyToMenuItem.setOnAction(event -> {
            if (onCopyToAction != null) {
                onCopyToAction.run();
            }
        });
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

    // ── ContextMenu Helpers ─────────────────────────────────────────────────
    private void setMenuItemVisible(MenuItem item, boolean visible) {
        item.setVisible(visible);
        item.setDisable(!visible);
    }
}