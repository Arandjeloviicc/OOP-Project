package com.fittrack.controller.measurements.components;

import com.fittrack.controller.common.BaseController;
import com.fittrack.controller.common.ResponsiveLayout;
import com.fittrack.dto.measurements.body.BodyMeasurementResponse;
import com.fittrack.dto.measurements.weight.WeightLogResponse;
import com.fittrack.model.profile.Gender;
import com.fittrack.model.profile.WeightGoal;
import com.fittrack.ui.graph.*;
import com.fittrack.util.NumberUtils;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class MeasurementChartController extends BaseController implements Initializable, ResponsiveLayout {

    // Root
    @FXML private VBox rootLayout;

    // Header
    @FXML private Label titleLabel;
    @FXML private Label valueLabel;
    @FXML private Label changeLabel;
    @FXML private Label periodSummaryLabel;

    // Controls
    @FXML private ComboBox<BodyMeasurementMetric> measurementSelector;

    @FXML private HBox periodSelector;
    @FXML private ToggleGroup periodGroup;
    @FXML private ComboBox<MeasurementChartPeriod> periodComboBox;

    // Chart
    @FXML private AreaChart<Number, Number> chart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;

    // Empty state
    @FXML private VBox emptyState;
    @FXML private Label emptyStateLabel;

    // Data
    private ChartMode mode;

    // WeightLog
    private List<WeightLogResponse> weightLogs = List.of();
    private WeightGoal weightGoal;

    // Body Measurement
    private List<BodyMeasurementResponse> bodyMeasurements = List.of();
    private Gender gender;

    // Default
    private BodyMeasurementMetric selectedBodyMetric = BodyMeasurementMetric.WAIST;
    private MeasurementChartPeriod selectedPeriod = MeasurementChartPeriod.ONE_MONTH;

    // Responsive
    private static final int NARROW_BREAKPOINT = 450;
    private static final PseudoClass NARROW = PseudoClass.getPseudoClass("narrow");

    // ── Initialization ─────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeChart();
        initializePeriodControls();
        initializeMeasurementSelector();

        initializeResponsiveWidthLayout(rootLayout, NARROW_BREAKPOINT);

        showEmptyState();
    }

    private void initializeChart() {
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);

        MeasurementChartAxisConfigurer.initialize(xAxis, yAxis);
    }

    private void initializePeriodControls() {
        periodComboBox.getItems().setAll(MeasurementChartPeriod.values());

        periodComboBox.setConverter(
            new StringConverter<>() {

                @Override
                public String toString(MeasurementChartPeriod period) {
                    return period == null
                            ? ""
                            : period.getLabel();
                }

                @Override
                public MeasurementChartPeriod fromString(String string) {
                    return null;
                }
            }
        );

        periodComboBox.setValue(selectedPeriod);

        periodGroup.selectedToggleProperty().addListener(
            (observable, oldToggle, newToggle) -> {

                if (newToggle == null) {
                    if (oldToggle != null) {
                        oldToggle.setSelected(true);
                    }

                    return;
                }

                MeasurementChartPeriod period = getPeriodFromToggle(newToggle);

                if (period != selectedPeriod) {
                    selectedPeriod = period;

                    if (periodComboBox.getValue() != period) {
                        periodComboBox.setValue(period);
                    }

                    renderData();
                }
            }
        );

        periodComboBox.valueProperty().addListener(
            (observable, oldValue, newValue) -> {

                if (newValue == null || newValue == selectedPeriod) {
                    return;
                }

                selectedPeriod = newValue;

                selectPeriodToggle(newValue);

                renderData();
            }
        );
    }

    private void initializeMeasurementSelector() {
        measurementSelector.setConverter(
            new StringConverter<>() {

                @Override
                public String toString(BodyMeasurementMetric metric) {
                    return metric == null
                            ? ""
                            : metric.getLabel();
                }

                @Override
                public BodyMeasurementMetric fromString(String string) {
                    return null;
                }
            }
        );

        measurementSelector.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == null || newValue == selectedBodyMetric) {
                        return;
                    }

                    selectedBodyMetric = newValue;

                    if (mode == ChartMode.BODY) {
                        renderData();
                    }
                }
        );
    }

    // ── Configuration ─────────────────────────────────────────────
    public void setWeightData(List<WeightLogResponse> weightLogs, WeightGoal weightGoal) {
        mode = ChartMode.WEIGHT;

        this.weightLogs = weightLogs == null
                ? List.of()
                : List.copyOf(weightLogs);

        this.weightGoal = weightGoal;

        measurementSelector.setVisible(false);
        measurementSelector.setManaged(false);

        renderData();
    }

    public void setBodyData(List<BodyMeasurementResponse> bodyMeasurements, Gender gender) {
        mode = ChartMode.BODY;

        this.bodyMeasurements = bodyMeasurements == null
                ? List.of()
                : List.copyOf(bodyMeasurements);

        this.gender = gender;

        configureBodyMeasurementSelector();

        renderData();
    }

    private void configureBodyMeasurementSelector() {
        measurementSelector.getItems().clear();

        measurementSelector.getItems().addAll(
                BodyMeasurementMetric.WAIST,
                BodyMeasurementMetric.NECK
        );

        if (gender == Gender.FEMALE) {
            measurementSelector.getItems().add(BodyMeasurementMetric.HIP);
        }

        if (!measurementSelector.getItems().contains(selectedBodyMetric)) {
            selectedBodyMetric = BodyMeasurementMetric.WAIST;
        }

        measurementSelector.setValue(selectedBodyMetric);

        measurementSelector.setVisible(true);
        measurementSelector.setManaged(true);
    }

    private MeasurementChartPeriod getPeriodFromToggle(Toggle toggle) {
        return MeasurementChartPeriod.valueOf(toggle.getUserData().toString());
    }

    private void selectPeriodToggle(MeasurementChartPeriod period) {
        for (Toggle toggle : periodGroup.getToggles()) {
            if (period.name().equals(toggle.getUserData().toString())) {
                toggle.setSelected(true);
                return;
            }
        }
    }

    // ── Rendering ──────────────────────────────────────────────────
    private void renderData() {
        List<ChartPoint> allPoints = getChartPoints()
                .stream()
                .sorted(Comparator.comparing(ChartPoint::date))
                .toList();

        List<ChartPoint> periodPoints = filterByPeriod(allPoints);

        updateHeader(allPoints, periodPoints);

        if (periodPoints.size() < 2) {
            chart.getData().clear();

            emptyStateLabel.setText(
                    allPoints.isEmpty()
                            ? "No measurements available."
                            : "Not enough data for this period."
            );

            showEmptyState();
            return;
        }

        MeasurementChartAxisConfigurer.configure(
                xAxis,
                yAxis,
                periodPoints
        );

        XYChart.Series<Number, Number> series = createSeries(periodPoints);

        chart.getData().setAll(List.of(series));

        showChart();

        MeasurementChartTooltipInstaller.install(
                series,
                getUnit()
        );
    }

    private XYChart.Series<Number, Number> createSeries(List<ChartPoint> points) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        for (ChartPoint point : points) {
            XYChart.Data<Number, Number> data =
                    new XYChart.Data<>(
                            point.date().toEpochDay(),
                            point.value()
                    );

            data.setExtraValue(point.date());

            series.getData().add(data);
        }

        return series;
    }

    // ── Data Mapping ──────────────────────────────────────────────────
    private List<ChartPoint> getChartPoints() {
        if (mode == null) {
            return List.of();
        }

        return switch (mode) {
            case WEIGHT -> getWeightChartPoints();
            case BODY -> getBodyChartPoints();
        };
    }

    private List<ChartPoint> getWeightChartPoints() {
        return weightLogs.stream()
            .map(log ->
                    new ChartPoint(
                            log.loggedAt()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate(),
                            log.weight()
                    )
            )
            .toList();
    }

    private List<ChartPoint> getBodyChartPoints() {
        return bodyMeasurements.stream()
                .map(measurement -> {
                    Double value = switch (selectedBodyMetric) {
                        case WAIST -> measurement.waist();
                        case NECK -> measurement.neck();
                        case HIP -> measurement.hip();
                    };

                    if (value == null) {
                        return null;
                    }

                    return new ChartPoint(
                            measurement.loggedAt()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate(),
                            value
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private List<ChartPoint> filterByPeriod(List<ChartPoint> points) {
        LocalDate fromDate = selectedPeriod.from(LocalDate.now());

        return points.stream()
                .filter(
                        point ->
                                !point.date()
                                        .isBefore(fromDate)
                )
                .toList();
    }

    // ── Header ─────────────────────────────────────────────────────
    private void updateHeader(List<ChartPoint> allPoints, List<ChartPoint> periodPoints) {
        updateTitle();

        if (allPoints.isEmpty()) {
            valueLabel.setText("—");
            changeLabel.setText("—");

            applyChangeStyle("neutral");
            return;
        }

        ChartPoint latest = allPoints.getLast();

        valueLabel.setText(NumberUtils.formatDecimal(latest.value()) + " " + getUnit());

        if (periodPoints.size() < 2) {
            changeLabel.setText("—");
            applyChangeStyle("neutral");
        } else {
            ChartPoint first = periodPoints.getFirst();
            ChartPoint last = periodPoints.getLast();

            double difference = last.value() - first.value();

            changeLabel.setText(NumberUtils.formatInputDecimalWithSign(difference) + " " + getUnit());

            applyChangeStyle(getChangeStyle(difference));
        }

        periodSummaryLabel.setText(
                "in selected period"
        );
    }

    private void updateTitle() {
        if (mode == null) {
            titleLabel.setText("Measurement trend");
            return;
        }

        switch (mode) {
            case WEIGHT -> titleLabel.setText("Weight trend");
            case BODY -> titleLabel.setText(selectedBodyMetric.getLabel() + " trend");
        }
    }

    private String getUnit() {
        return mode == ChartMode.WEIGHT
                ? "kg"
                : "cm";
    }

    private void applyChangeStyle(String styleClass) {
        changeLabel.getStyleClass().removeAll(
                "favorable",
                "unfavorable",
                "neutral"
        );

        changeLabel.getStyleClass().add(styleClass);
    }

    private String getChangeStyle(double difference) {
        if (mode == ChartMode.BODY
                || weightGoal == null
                || Math.abs(difference) < 0.001) {

            return "neutral";
        }

        return switch (weightGoal) {
            case LOSE_WEIGHT ->
                    difference < 0
                            ? "favorable"
                            : "unfavorable";

            case GAIN_WEIGHT ->
                    difference > 0
                            ? "favorable"
                            : "unfavorable";

            case MAINTAIN_WEIGHT -> "neutral";
        };
    }

    // ── State ──────────────────────────────────────────────────────
    private void showChart() {
        chart.setVisible(true);
        chart.setManaged(true);

        emptyState.setVisible(false);
        emptyState.setManaged(false);
    }

    private void showEmptyState() {
        chart.setVisible(false);
        chart.setManaged(false);

        emptyState.setVisible(true);
        emptyState.setManaged(true);
    }

    // ── Responsive ─────────────────────────────────────────────────
    @Override
    public void updateWidthLayout(boolean narrow) {
        rootLayout.pseudoClassStateChanged(NARROW, narrow);

        periodSelector.setVisible(!narrow);
        periodSelector.setManaged(!narrow);

        periodComboBox.setVisible(narrow);
        periodComboBox.setManaged(narrow);
    }
}