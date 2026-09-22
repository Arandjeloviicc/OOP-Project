package com.fittrack.controller.measurements.components;

import com.fittrack.dto.measurements.weight.WeightLogResponse;
import com.fittrack.model.profile.WeightGoal;
import com.fittrack.util.NumberUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class WeightLogItemController {

    @FXML private HBox rootLayout;
    @FXML private Label dateLabel;
    @FXML private Label changeLabel;
    @FXML private Label weightLabel;
    @FXML private Button deleteButton;

    // Actions
    private Runnable onEditAction;
    private Runnable onDeleteAction;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.uuuu");

    // ── Configuration ────────────────────────────────────────────────
    public void setData(WeightLogResponse weightLog, Double previousWeight, WeightGoal goalType) {
        dateLabel.setText(
                weightLog.loggedAt()
                        .atZone(ZoneId.systemDefault())
                        .format(DATE_FORMATTER)
        );

        weightLabel.setText(NumberUtils.formatDecimal(weightLog.weight()) + " kg");

        updateChangeLabel(weightLog.weight(), previousWeight, goalType);
    }

    public void setOnEditAction(Runnable onEditAction) {
        this.onEditAction = onEditAction;
    }

    public void setOnDeleteAction(Runnable onDeleteAction) {
        this.onDeleteAction = onDeleteAction;
    }

    // ── Button Actions ────────────────────────────────────────────────
    @FXML
    private void handleEdit(MouseEvent event) {
        if (isClickOnButton(event)) {
            return;
        }

        if (onEditAction != null) {
            onEditAction.run();
        }
    }

    @FXML
    private void handleDelete() {
        if (onDeleteAction != null) {
            onDeleteAction.run();
        }
    }

    // ── Helpers ─────────────────────────────────────────────────
    private void updateChangeLabel(double currentWeight, Double previousWeight, WeightGoal goalType) {
        if (previousWeight == null) {
            changeLabel.setVisible(false);
            changeLabel.setManaged(false);
            return;
        }

        double difference = currentWeight - previousWeight;

        changeLabel.setVisible(true);
        changeLabel.setManaged(true);

        changeLabel.getStyleClass().removeAll(
                "weight-change-favorable",
                "weight-change-unfavorable",
                "weight-change-neutral"
        );

        if (Math.abs(difference) < 0.05) {
            changeLabel.setText("No change since last");
            changeLabel.getStyleClass().add(
                    "weight-change-neutral"
            );
            return;
        }

        changeLabel.setText(NumberUtils.formatInputDecimalWithSign(difference) + " kg since last");

        switch (goalType) {
            case LOSE_WEIGHT -> {
                if (difference < 0) {
                    changeLabel.getStyleClass().add(
                            "weight-change-favorable"
                    );
                } else {
                    changeLabel.getStyleClass().add(
                            "weight-change-unfavorable"
                    );
                }
            }

            case GAIN_WEIGHT -> {
                if (difference > 0) {
                    changeLabel.getStyleClass().add(
                            "weight-change-favorable"
                    );
                } else {
                    changeLabel.getStyleClass().add(
                            "weight-change-unfavorable"
                    );
                }
            }

            case MAINTAIN_WEIGHT ->
                    changeLabel.getStyleClass().add(
                            "weight-change-neutral"
                    );
        }
    }

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
