package com.fittrack.model.measurement;

import com.fittrack.dto.measurements.body.BodyMeasurementResponse;
import com.fittrack.model.profile.Gender;

import java.util.List;

public record BodyMeasurementHistoryData(
        List<BodyMeasurementResponse> measurements,
        Gender gender
) {}