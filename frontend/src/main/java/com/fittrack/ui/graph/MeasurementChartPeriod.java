package com.fittrack.ui.graph;

import java.time.LocalDate;

public enum MeasurementChartPeriod {

    SEVEN_DAYS("7 days") {
        @Override
        public LocalDate from(LocalDate today) {
            return today.minusDays(7);
        }
    },

    ONE_MONTH("1 month") {
        @Override
        public LocalDate from(LocalDate today) {
            return today.minusMonths(1);
        }
    },

    THREE_MONTHS("3 months") {
        @Override
        public LocalDate from(LocalDate today) {
            return today.minusMonths(3);
        }
    },

    SIX_MONTHS("6 months") {
        @Override
        public LocalDate from(LocalDate today) {
            return today.minusMonths(6);
        }
    },

    ONE_YEAR("1 year") {
        @Override
        public LocalDate from(LocalDate today) {
            return today.minusYears(1);
        }
    },

    ALL("All time") {
        @Override
        public LocalDate from(LocalDate today) {
            return LocalDate.MIN;
        }
    };

    private final String label;

    MeasurementChartPeriod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public abstract LocalDate from(LocalDate today);
}