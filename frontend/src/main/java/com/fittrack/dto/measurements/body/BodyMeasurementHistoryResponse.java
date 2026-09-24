package com.fittrack.dto.measurements.body;

import java.util.List;

public record BodyMeasurementHistoryResponse(
        List<BodyMeasurementResponse> measurements,
        String gender
) {}