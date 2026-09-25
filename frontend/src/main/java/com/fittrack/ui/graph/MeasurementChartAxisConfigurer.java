package com.fittrack.ui.graph;

import javafx.scene.chart.NumberAxis;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MeasurementChartAxisConfigurer {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "MMM d",
                    Locale.ENGLISH
            );

    private MeasurementChartAxisConfigurer() {}

    public static void initialize(NumberAxis xAxis, NumberAxis yAxis) {
        xAxis.setForceZeroInRange(false);
        yAxis.setForceZeroInRange(false);

        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(false);

        configureXFormatter(xAxis);
        configureYFormatter(yAxis);
    }

    public static void configure(NumberAxis xAxis, NumberAxis yAxis, List<ChartPoint> points) {
        configureXAxis(xAxis, points);
        configureYAxis(yAxis, points);
    }

    private static void configureXFormatter(NumberAxis xAxis) {
        xAxis.setTickLabelFormatter(
            new StringConverter<>() {
                @Override
                public String toString(Number value) {
                    if (value == null) {
                        return "";
                    }

                    return LocalDate
                            .ofEpochDay(value.longValue())
                            .format(DATE_FORMATTER);
                }

                @Override
                public Number fromString(String string) {
                    return null;
                }
            }
        );
    }

    private static void configureYFormatter(NumberAxis yAxis) {
        yAxis.setTickLabelFormatter(
            new StringConverter<>() {
                @Override
                public String toString(Number value) {
                    if (value == null) {
                        return "";
                    }

                    double number = value.doubleValue();

                    if (Math.abs(number - Math.rint(number)) < 0.001) {
                        return String.format(
                                Locale.ENGLISH,
                                "%.0f",
                                number
                        );
                    }

                    return String.format(
                            Locale.ENGLISH,
                            "%.1f",
                            number
                    );
                }

                @Override
                public Number fromString(String string) {
                    return null;
                }
            }
        );
    }

    private static void configureXAxis(NumberAxis xAxis, List<ChartPoint> points) {
        long minimumDay = points.getFirst().date().toEpochDay();

        long maximumDay = points.getLast().date().toEpochDay();

        long dataSpan =
                Math.max(
                        1,
                        maximumDay - minimumDay
                );

        long padding =
                Math.max(
                        2,
                        Math.round(dataSpan * 0.1)
                );

        double lowerBound = (double) minimumDay - padding;
        double upperBound = (double) maximumDay + padding;

        xAxis.setLowerBound(lowerBound);
        xAxis.setUpperBound(upperBound);

        xAxis.setTickUnit(calculateDateTickUnit(upperBound - lowerBound));
    }

    private static void configureYAxis(NumberAxis yAxis, List<ChartPoint> points) {
        double minimum = points.stream()
                .mapToDouble(ChartPoint::value)
                .min()
                .orElse(0);

        double maximum = points.stream()
                .mapToDouble(ChartPoint::value)
                .max()
                .orElse(0);

        double range = maximum - minimum;

        double padding =
                Math.max(
                        0.75,
                        range * 0.2
                );

        double rawLower = minimum - padding;
        double rawUpper = maximum + padding;

        double tickUnit = calculateNiceTickUnit(rawUpper - rawLower);

        double lowerBound = Math.floor(rawLower / tickUnit) * tickUnit;
        double upperBound = Math.ceil(rawUpper / tickUnit) * tickUnit;

        if (Double.compare(lowerBound, upperBound) == 0) {
            lowerBound -= 1;
            upperBound += 1;
        }

        yAxis.setLowerBound(lowerBound);
        yAxis.setUpperBound(upperBound);
        yAxis.setTickUnit(tickUnit);
    }

    private static double calculateDateTickUnit(double daySpan) {
        if (daySpan <= 9) return 2;
        if (daySpan <= 35) return 7;
        if (daySpan <= 100) return 21;
        if (daySpan <= 200) return 45;
        if (daySpan <= 400) return 90;

        return Math.max(
                90,
                Math.ceil(daySpan / 5.0)
        );
    }

    private static double calculateNiceTickUnit(double range) {
        double roughTick = range / 4.0;

        if (roughTick <= 0.5) return 0.5;
        if (roughTick <= 1) return 1;
        if (roughTick <= 2) return 2;
        if (roughTick <= 5) return 5;
        if (roughTick <= 10) return 10;

        return Math.ceil(roughTick / 10) * 10;
    }
}