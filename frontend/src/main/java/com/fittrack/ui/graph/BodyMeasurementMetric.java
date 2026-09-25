package com.fittrack.ui.graph;

public enum BodyMeasurementMetric {
    WAIST("Waist"),
    NECK("Neck"),
    HIP("Hip");

    private final String label;

    BodyMeasurementMetric(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}