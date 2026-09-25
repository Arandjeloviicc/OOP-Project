package com.fittrack.controller.profile.components;

import com.fittrack.controller.common.BaseController;
import com.fittrack.util.NumberUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class ProfileWeightCardController extends BaseController {

    @FXML private Button addWeightButton;
    @FXML private Label currentWeightLabel;
    @FXML private VBox progressContent;
    @FXML private Label startWeightLabel;
    @FXML private Label goalWeightLabel;
    @FXML private ProgressBar weightProgressBar;
    @FXML private Label progressMessageLabel;

    // Actions
    private Runnable onAddAction;

    // ── Configuration ──────────────────────────────────────────
    public void setOnAddAction(Runnable onAddAction) {
        this.onAddAction = onAddAction;
    }

    public void setData(double currentWeight, Double startWeight, Double goalWeight) {
        currentWeightLabel.setText(NumberUtils.formatDecimal(currentWeight));

        if (startWeight == null
            || goalWeight == null
            || Double.compare(startWeight, goalWeight) == 0) {
            setProgressVisible(false);
            return;
        }

        setProgressVisible(true);

        startWeightLabel.setText(formatWeight(startWeight));
        goalWeightLabel.setText(formatWeight(goalWeight));

        updateProgress(currentWeight, startWeight, goalWeight);
    }

    private void updateProgress(double currentWeight, double startWeight, double goalWeight) {
        boolean losingWeight = goalWeight < startWeight;

        double totalChange = Math.abs(goalWeight - startWeight);

        double completedChange;
        double remainingChange;

        if (losingWeight) {
            completedChange = Math.max(0, startWeight - currentWeight);
            remainingChange = Math.max(0, currentWeight - goalWeight);
        } else {
            completedChange = Math.max(0, currentWeight - startWeight);
            remainingChange = Math.max(0, goalWeight - currentWeight);
        }

        double progress = Math.min(
                completedChange / totalChange,
                1.0
        );

        weightProgressBar.setProgress(progress);

        progressMessageLabel.setText(
                formatWeight(completedChange)
                        + (losingWeight ? " lost · " : " gained · ")
                        + formatWeight(remainingChange)
                        + " to go"
        );
    }

    private String formatWeight(double weight) {
        return NumberUtils.formatDecimal(weight) + " kg";
    }

    private void setProgressVisible(boolean visible) {
        progressContent.setVisible(visible);
        progressContent.setManaged(visible);
    }

    // ── Button Actions ──────────────────────────────────────────
    @FXML
    private void handleAdd() {
        if (onAddAction != null) {
            onAddAction.run();
        }
    }
}