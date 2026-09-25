package com.fittrack.ui.graph;

import com.fittrack.util.NumberUtils;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MeasurementChartTooltipInstaller {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "d MMM uuuu",
                    Locale.ENGLISH
            );

    private MeasurementChartTooltipInstaller() {}

    public static void install(XYChart.Series<Number, Number> series, String unit) {
        Platform.runLater(
                () -> installNow(series, unit)
        );
    }

    private static void installNow(XYChart.Series<Number, Number> series, String unit) {
        for (XYChart.Data<Number, Number> data : series.getData()) {
            Node node = data.getNode();

            if (node == null) {
                continue;
            }

            LocalDate date = (LocalDate) data.getExtraValue();

            double value = data.getYValue().doubleValue();

            Tooltip tooltip = new Tooltip(
                    NumberUtils.formatDecimal(value)
                            + " "
                            + unit
                            + "\n"
                            + date.format(DATE_FORMATTER)
            );

            tooltip.setShowDelay(Duration.millis(100));

            tooltip.getStyleClass().add(
                    "chart-point-tooltip"
            );

            Tooltip.install(node, tooltip);
        }
    }
}