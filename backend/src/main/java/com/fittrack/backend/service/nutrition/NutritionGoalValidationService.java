package com.fittrack.backend.service.nutrition;

import com.fittrack.backend.entity.profile.WeightGoal;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class NutritionGoalValidationService {

    private static final Set<Double> ALLOWED_WEEKLY_GOALS = Set.of(0.25, 0.5, 0.75, 1.0);

    public void validate(WeightGoal goalType, Double goalWeight, Double weeklyGoal, double currentWeight) {
        switch (goalType) {
            case LOSE_WEIGHT -> {
                validateWeeklyGoal(weeklyGoal);

                if (goalWeight != null && goalWeight >= currentWeight) {
                    throw new IllegalArgumentException("Goal weight must be lower than current weight.");
                }
            }

            case GAIN_WEIGHT -> {
                validateWeeklyGoal(weeklyGoal);

                if (goalWeight != null && goalWeight <= currentWeight) {
                    throw new IllegalArgumentException("Goal weight must be higher than current weight.");
                }
            }

            case MAINTAIN_WEIGHT -> {
                if (goalWeight != null || weeklyGoal != null) {
                    throw new IllegalArgumentException("Maintain weight goal must not have goal weight or weekly goal.");
                }
            }
        }
    }

    private void validateWeeklyGoal(Double weeklyGoal) {
        if (weeklyGoal == null || !ALLOWED_WEEKLY_GOALS.contains(weeklyGoal)) {
            throw new IllegalArgumentException("Weekly goal must be 0.25, 0.5, 0.75 or 1.0.");
        }
    }
}
