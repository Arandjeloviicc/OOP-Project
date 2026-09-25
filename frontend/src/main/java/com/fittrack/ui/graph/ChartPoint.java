package com.fittrack.ui.graph;

import java.time.LocalDate;

public record ChartPoint(
        LocalDate date,
        double value
) {}