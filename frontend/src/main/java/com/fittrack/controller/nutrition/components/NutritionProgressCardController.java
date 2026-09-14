package com.fittrack.controller.nutrition.components;

import com.fittrack.util.NumberUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class NutritionProgressCardController {

    @FXML private Label titleLabel;
    @FXML private Label currentValueLabel;
    @FXML private Label unitLabel;
    @FXML private Label goalValueLabel;
    @FXML private HBox remainingContainer;
    @FXML private Label remainingValueLabel;
    @FXML private Label remainingStatusLabel;
    @FXML private StackPane progressBarContainer;
    @FXML private ProgressBar progressBar;
    @FXML private Region overflowOverlay;

    // ProgressBar Overflow
    private final Rectangle overflowClip = new Rectangle();

    public void initialize() {
        initializeProgressBarOverflow();
    }

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
            setOverflow(0);
            return;
        }

        double progress = current / goal;
        progressBar.setProgress(Math.min(progress, 1.0));

        double overflowFraction = progress > 1.0
                ? Math.min(progress - 1.0, 1.0)
                : 0;

        setOverflow(overflowFraction);
    }

    private void initializeProgressBarOverflow() {
        Rectangle containerClip = new Rectangle();

        containerClip.widthProperty().bind(
                progressBarContainer.widthProperty()
        );

        containerClip.heightProperty().bind(
                progressBarContainer.heightProperty()
        );

        progressBarContainer.setClip(containerClip);

        overflowOverlay.setManaged(true);

        overflowClip.heightProperty().bind(
                overflowOverlay.heightProperty()
        );

        overflowClip.xProperty().bind(
                overflowOverlay.widthProperty()
                        .subtract(overflowClip.widthProperty())
        );

        overflowOverlay.setClip(overflowClip);
    }

    private void setOverflow(double fraction) {
        double clampedFraction = Math.clamp(
                fraction,
                0.0,
                1.0
        );

        overflowOverlay.setVisible(clampedFraction > 0);

        overflowClip.widthProperty().unbind();

        if (clampedFraction > 0) {
            overflowClip.widthProperty().bind(
                    overflowOverlay.widthProperty()
                            .multiply(clampedFraction)
            );
        } else {
            overflowClip.setWidth(0);
        }
    }

    // ── Remaining ─────────────────────────────────────────────────
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