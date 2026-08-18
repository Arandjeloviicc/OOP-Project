package com.fittrack.controller.nutrition.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
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

    private static final Color CARBS_COLOR = Color.web("#FFC857");
    private static final Color FAT_COLOR = Color.web("#6BCB77");
    private static final Color PROTEIN_COLOR = Color.web("#FF6B6B");
    private static final Color EMPTY_COLOR = Color.web("rgba(255,255,255,0.08)");

    private double lastCarbs;
    private double lastFat;
    private double lastProtein;

    @FXML
    public void initialize() {
        chartPane.widthProperty().addListener((obs, oldVal, newVal) -> drawChart(lastCarbs, lastFat, lastProtein));
        chartPane.heightProperty().addListener((obs, oldVal, newVal) -> drawChart(lastCarbs, lastFat, lastProtein));
    }

    public void setData(double calories, double carbs, double fat, double protein) {
        caloriesValueLabel.setText(String.valueOf(Math.round(calories)));

        carbsLabel.setText(Math.round(carbs) + " g");
        fatLabel.setText(Math.round(fat) + " g");
        proteinLabel.setText(Math.round(protein) + " g");

        this.lastCarbs = carbs;
        this.lastFat = fat;
        this.lastProtein = protein;

        drawChart(carbs, fat, protein);
    }

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
            chartPane.getChildren().add(createArcSegment(centerX, centerY, radius, 0, 360, EMPTY_COLOR));
            return;
        }

        double carbsAngle = 360 * (carbsCalories / total);
        double fatAngle = 360 * (fatCalories / total);
        double proteinAngle = 360 * (proteinCalories / total);

        double startAngle = 90;

        Arc carbsArc = createArcSegment(centerX, centerY, radius, startAngle, -carbsAngle, CARBS_COLOR);
        startAngle -= carbsAngle;

        Arc fatArc = createArcSegment(centerX, centerY, radius, startAngle, -fatAngle, FAT_COLOR);
        startAngle -= fatAngle;

        Arc proteinArc = createArcSegment(centerX, centerY, radius, startAngle, -proteinAngle, PROTEIN_COLOR);

        chartPane.getChildren().addAll(carbsArc, fatArc, proteinArc);
    }

    private Arc createArcSegment(double centerX, double centerY, double radius, double startAngle, double length, Color color) {
        Arc arc = new Arc(centerX, centerY, radius, radius, startAngle, length);
        arc.setType(ArcType.OPEN);
        arc.setFill(null);
        arc.setStroke(color);
        arc.setStrokeWidth(STROKE_WIDTH);
        arc.setStrokeLineCap(StrokeLineCap.BUTT);
        return arc;
    }
}
