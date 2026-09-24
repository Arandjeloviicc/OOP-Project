package com.fittrack.controller.measurements.components;

import com.fittrack.controller.common.BaseController;
import com.fittrack.dto.measurements.body.BodyMeasurementResponse;
import com.fittrack.util.NumberUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class BodyMeasurementItemController extends BaseController {

    // Root
    @FXML private VBox rootLayout;

    // Info and Delete
    @FXML private Label dateLabel;
    @FXML private Button deleteButton;

    // Measurements
    @FXML private Label waistLabel;
    @FXML private Label neckLabel;
    @FXML private VBox hipBox;
    @FXML private Label hipLabel;

    // Actions
    private Runnable onEditAction;
    private Runnable onDeleteAction;

    // Date
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d.M.uuuu");

    // ── Configuration ────────────────────────────────────────────────
    public void setData(BodyMeasurementResponse measurement) {
        dateLabel.setText(
                measurement.loggedAt()
                        .atZone(ZoneId.systemDefault())
                        .format(DATE_FORMATTER)
        );

        waistLabel.setText(NumberUtils.formatDecimal(measurement.waist()) + " cm");

        neckLabel.setText(NumberUtils.formatDecimal(measurement.neck()) + " cm");

        updateHip(measurement.hip());
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
    private void updateHip(Double hip) {
        boolean hasHip = hip != null;

        setVisible(hipBox, hasHip);

        if (hasHip) {
            hipLabel.setText(NumberUtils.formatDecimal(hip) + " cm");
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