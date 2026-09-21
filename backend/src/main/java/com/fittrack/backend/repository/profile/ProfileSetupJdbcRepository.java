package com.fittrack.backend.repository.profile;

import com.fittrack.backend.dto.nutrition.goal.NutritionTargets;
import com.fittrack.backend.dto.profile.ProfileSetupRequest;
import com.fittrack.backend.exception.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public class ProfileSetupJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProfileSetupJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void completeProfile(ProfileSetupRequest request, NutritionTargets targets, LocalDate startDate) {
        // valid_user - proverava da korisnik postoji
        // inserted_profile - kreira profil korisnika (samo ako valid_user nije prazan)
        // inserted_weight - koristi user_id iz inserted_profile da odmah upise prvi zapis tezine (isti unos koji je deo profila); ako inserted_profile nista nije vratio, ni ovo se ne izvrsava
        // INSERT nutrition_goals - koristi user_id iz inserted_weight da kreira ciljeve ishrane (kalorije/makrosi) izracunate na osnovu unetih podataka; ako inserted_weight nista nije vratio, ni ovo se ne izvrsava

        String sql = """
            WITH valid_user AS (
                SELECT id
                FROM users
                WHERE id = ?
            ),
            inserted_profile AS (
                INSERT INTO user_profiles (
                     user_id,
                     first_name,
                     last_name,
                     date_of_birth,
                     gender,
                     height,
                     created_at
                 )
                SELECT
                     vu.id,
                     ?,
                     ?,
                     ?,
                     ?,
                     ?,
                     CURRENT_TIMESTAMP
                FROM valid_user vu
                RETURNING user_id
            ),
            inserted_weight AS (
                INSERT INTO weight_logs (
                    user_id,
                    logged_at,
                    weight,
                    created_at
                )
                SELECT
                    ip.user_id,
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP
                FROM inserted_profile ip
                RETURNING user_id, weight, logged_at
            )
            INSERT INTO nutrition_goals (
                user_id,
                activity_level,
                goal_type,
                goal_weight,
                weekly_goal,
                target_calories,
                target_protein,
                target_carbs,
                target_fat,
                progress_start_weight,
                progress_started_at,
                start_date,
                created_at
            )
            SELECT
                iw.user_id,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                iw.weight,
                iw.logged_at,
                ?,
                CURRENT_TIMESTAMP
            FROM inserted_weight iw
            """;

        int inserted = jdbcTemplate.update(
                sql,
                request.userId(),

                request.firstName(),
                request.lastName(),
                request.dateOfBirth(),
                request.gender().name(),
                request.height(),

                request.weight(),

                request.activityLevel().name(),
                request.goalType().name(),
                request.goalWeight(),
                request.weeklyGoal(),

                targets.calories(),
                targets.protein(),
                targets.carbs(),
                targets.fat(),

                startDate
        );

        if (inserted == 0) {
            throw new ResourceNotFoundException("User not found.");
        }
    }
}
