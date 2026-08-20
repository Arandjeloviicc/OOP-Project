package com.fittrack.controller.nutrition.components;

import com.fittrack.util.NumberUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;

public class NutritionProgressCardController {

    @FXML private Label titleLabel;
    @FXML private Label currentValueLabel;
    @FXML private Label unitLabel;
    @FXML private Label goalValueLabel;
    @FXML private HBox remainingContainer;
    @FXML private Label remainingValueLabel;
    @FXML private Label remainingStatusLabel;
    @FXML private ProgressBar progressBar;

    // ── Set Data ─────────────────────────────────────────────────
    public void setData(String title, double current, double goal, String unit, boolean showRemaining) {
        titleLabel.setText(title);

        currentValueLabel.setText(NumberUtils.formatWhole(current));
        unitLabel.setText(unit);
        goalValueLabel.setText(" / " + NumberUtils.formatWhole(goal));

        updateProgress(current, goal);
        updateRemaining(current, goal, showRemaining);
    }

    // ── Progress Bar ─────────────────────────────────────────────────
    public void setProgressStyle(String styleClass) {
        progressBar.getStyleClass().add(styleClass);
    }

    private void updateProgress(double current, double goal) {
        if (goal <= 0) {
            progressBar.setProgress(0);
            return;
        }

        double progress = current / goal;
        progressBar.setProgress(Math.min(progress, 1.0));
    }

    private void updateRemaining(double current, double goal, boolean showingRemaining) {
        remainingContainer.setVisible(showingRemaining);
        remainingContainer.setManaged(showingRemaining);

        if (!showingRemaining) {
            return;
        }

        int difference = (int) Math.abs(goal - current);

        remainingValueLabel.setText(NumberUtils.formatWhole(difference));

        if (current <= goal) {
            remainingStatusLabel.setText("left");
        } else {
            remainingStatusLabel.setText("over");
        }
    }
}