package com.fittrack.controller.nutrition.components;

import com.fittrack.util.NumberUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;

public class NutritionMacroPreviewController {

    // Fields
    @FXML private Pane chartPane;
    @FXML private Label caloriesValueLabel;
    @FXML private Label carbsLabel;
    @FXML private Label fatLabel;
    @FXML private Label proteinLabel;

    private static final double STROKE_WIDTH = 10;

    private static final String CARBS_STYLE = "macro-arc-carbs";
    private static final String FAT_STYLE = "macro-arc-fat";
    private static final String PROTEIN_STYLE = "macro-arc-protein";
    private static final String EMPTY_STYLE = "macro-arc-empty";

    private double lastCarbs;
    private double lastFat;
    private double lastProtein;

    @FXML
    public void initialize() {
        chartPane.widthProperty().addListener((obs, oldVal, newVal) -> drawChart(lastCarbs, lastFat, lastProtein));
        chartPane.heightProperty().addListener((obs, oldVal, newVal) -> drawChart(lastCarbs, lastFat, lastProtein));
    }

    // ── Set Data ─────────────────────────────────────────────────
    public void setData(double calories, double carbs, double fat, double protein) {
        caloriesValueLabel.setText(NumberUtils.formatWhole(Math.round(calories)));

        carbsLabel.setText(NumberUtils.formatWhole(Math.round(carbs)) + " g");
        fatLabel.setText(NumberUtils.formatWhole(Math.round(fat)) + " g");
        proteinLabel.setText(NumberUtils.formatWhole(Math.round(protein)) + " g");

        this.lastCarbs = carbs;
        this.lastFat = fat;
        this.lastProtein = protein;

        drawChart(carbs, fat, protein);
    }

    // ── Donut Pie Chart ─────────────────────────────────────────────────
    private void drawChart(double carbs, double fat, double protein) {
        double width = chartPane.getWidth();
        double height = chartPane.getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        double centerX = width / 2;
        double centerY = height / 2;
        double radius = (Math.min(width, height) / 2) - (STROKE_WIDTH / 2);

        double carbsCalories = carbs * 4;
        double fatCalories = fat * 9;
        double proteinCalories = protein * 4;
        double total = carbsCalories + fatCalories + proteinCalories;

        chartPane.getChildren().clear();

        if (total <= 0) {
            chartPane.getChildren().add(createArcSegment(centerX, centerY, radius, 0, 360, EMPTY_STYLE));
            return;
        }

        double carbsAngle = 360 * (carbsCalories / total);
        double fatAngle = 360 * (fatCalories / total);
        double proteinAngle = 360 * (proteinCalories / total);

        double startAngle = 90;

        Arc carbsArc = createArcSegment(centerX, centerY, radius, startAngle, -carbsAngle, CARBS_STYLE);
        startAngle -= carbsAngle;

        Arc fatArc = createArcSegment(centerX, centerY, radius, startAngle, -fatAngle, FAT_STYLE);
        startAngle -= fatAngle;

        Arc proteinArc = createArcSegment(centerX, centerY, radius, startAngle, -proteinAngle, PROTEIN_STYLE);

        chartPane.getChildren().addAll(carbsArc, fatArc, proteinArc);
    }

    private Arc createArcSegment(double centerX, double centerY, double radius, double startAngle, double length, String styleClass) {
        Arc arc = new Arc(centerX, centerY, radius, radius, startAngle, length);

        arc.setType(ArcType.OPEN);
        arc.setFill(null);
        arc.setStrokeWidth(STROKE_WIDTH);
        arc.setStrokeLineCap(StrokeLineCap.BUTT);

        arc.getStyleClass().add(styleClass);

        return arc;
    }
}
