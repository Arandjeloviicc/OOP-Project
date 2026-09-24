package com.fittrack.controller.profile.components;

import com.fittrack.controller.common.BaseController;
import com.fittrack.util.NumberUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProfileTargetsCardController extends BaseController {

    @FXML private Label caloriesLabel;
    @FXML private Label carbsLabel;
    @FXML private Label fatLabel;
    @FXML private Label proteinLabel;

    public void setData(int calories, double carbs, double fat, double protein) {
        caloriesLabel.setText(NumberUtils.formatWhole(calories));

        carbsLabel.setText(formatGrams(carbs));
        fatLabel.setText(formatGrams(fat));
        proteinLabel.setText(formatGrams(protein));
    }

    private String formatGrams(double value) {
        return NumberUtils.formatWhole(value) + " g";
    }
}