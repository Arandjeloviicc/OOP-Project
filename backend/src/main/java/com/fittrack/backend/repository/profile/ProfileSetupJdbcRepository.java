package com.fittrack.backend.repository.profile;

import com.fittrack.backend.dto.profile.ProfileSetupRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileSetupJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProfileSetupJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void completeProfile(ProfileSetupRequest request) {
        // valid_user - proverava da korisnik postoji
        // inserted_profile - kreira profil korisnika (samo ako valid_user nije prazan)
        // INSERT weight_logs - koristi user_id iz inserted_profile da odmah upise prvi zapis tezine (isti unos koji je deo profila), ako inserted_profile nista nije vratio, ni ovo se ne izvrsava

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
                        activity_level,
                        goal_type,
                        goal_weight,
                        weekly_goal,
                        created_at
                    )
                    SELECT
                        vu.id,
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        CURRENT_TIMESTAMP
                    FROM valid_user vu
                    RETURNING user_id
                )
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
                """;

        int inserted = jdbcTemplate.update(
                sql,
                request.userId(),
                request.firstName(),
                request.lastName(),
                request.dateOfBirth(),
                request.gender().name(),
                request.height(),
                request.activityLevel().name(),
                request.goalType().name(),
                request.goalWeight(),
                request.weeklyGoal(),
                request.weight()
        );

        if (inserted == 0) {
            throw new IllegalArgumentException("User not found.");
        }
    }
}
