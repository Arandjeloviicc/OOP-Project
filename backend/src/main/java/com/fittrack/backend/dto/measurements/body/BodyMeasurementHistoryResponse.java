package com.fittrack.backend.dto.measurements.body;

import com.fittrack.backend.entity.profile.Gender;

import java.util.List;

public record BodyMeasurementHistoryResponse(
        List<BodyMeasurementResponse> measurements,
        Gender gender
) {}