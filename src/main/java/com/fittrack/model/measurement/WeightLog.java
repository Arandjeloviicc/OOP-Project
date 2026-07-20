package com.fittrack.model.measurement;

import java.time.LocalDate;

public record WeightLog(
        int userId,
        LocalDate loggedDate,
        double weight
) {
}
