package com.fittrack.service.calculators;

import com.fittrack.api.calculators.CalculatorsApi;
import com.fittrack.dto.calculators.CalculatorsResponse;
import com.fittrack.model.calculators.CalculatorsData;
import com.fittrack.model.profile.ActivityLevel;
import com.fittrack.model.profile.Gender;
import com.fittrack.session.UserSession;

import java.time.LocalDate;
import java.time.Period;

public class CalculatorsService {

    private final CalculatorsApi calculatorsApi;

    public CalculatorsService() {
        this.calculatorsApi = new CalculatorsApi();
    }

    public CalculatorsData getCalculatorsData() {
        CalculatorsResponse response = calculatorsApi.getCalculatorsData(currentUserId());

        int age = Period.between(
                response.dateOfBirth(),
                LocalDate.now()
        ).getYears();

        return new CalculatorsData(
                response.height(),
                response.weight(),
                age,
                Gender.valueOf(response.gender()),
                ActivityLevel.valueOf(response.activityLevel()),
                response.neck(),
                response.waist(),
                response.hip()
        );
    }

    // ── Helpers ─────────────────────────────────────────────
    private Integer currentUserId() {
        return UserSession.getInstance().requireCurrentUser().id();
    }
}