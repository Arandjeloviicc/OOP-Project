package com.fittrack.backend.repository.profile;

import com.fittrack.backend.entity.profile.ActivityLevel;
import com.fittrack.backend.entity.profile.Gender;
import com.fittrack.backend.entity.profile.WeightGoal;
import com.fittrack.backend.repository.profile.projection.ProfileData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProfileJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProfileJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<ProfileData> findByUserId(Integer userId) {
        String sql = """
                SELECT
                    u.username,
                    u.email,

                    up.first_name,
                    up.last_name,
                    up.date_of_birth,
                    up.gender,
                    up.height,

                    current_weight.weight AS current_weight,
                    start_weight.weight AS start_weight,

                    ng.goal_type,
                    ng.goal_weight,
                    ng.weekly_goal,
                    ng.activity_level,

                    ng.target_calories,
                    ng.target_carbs,
                    ng.target_fat,
                    ng.target_protein

                FROM users u

                JOIN user_profiles up
                    ON up.user_id = u.id

                JOIN nutrition_goals ng
                    ON ng.user_id = u.id
                   AND ng.end_date IS NULL

                LEFT JOIN LATERAL (
                    SELECT wl.weight
                    FROM weight_logs wl
                    WHERE wl.user_id = u.id
                    ORDER BY wl.logged_at DESC, wl.id DESC
                    LIMIT 1
                ) current_weight ON TRUE

                LEFT JOIN LATERAL (
                    SELECT wl.weight
                    FROM weight_logs wl
                    WHERE wl.user_id = u.id
                      AND wl.logged_at <= ng.created_at
                    ORDER BY wl.logged_at DESC, wl.id DESC
                    LIMIT 1
                ) start_weight ON TRUE

                WHERE u.id = ?
                """;

        List<ProfileData> results = jdbcTemplate.query(
                sql,
                (resultSet, _) -> new ProfileData(
                        resultSet.getString("username"),
                        resultSet.getString("email"),

                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getObject("date_of_birth", java.time.LocalDate.class),
                        Gender.valueOf(resultSet.getString("gender")),
                        resultSet.getDouble("height"),

                        resultSet.getObject("current_weight", Double.class),
                        resultSet.getObject("start_weight", Double.class),

                        WeightGoal.valueOf(resultSet.getString("goal_type")),
                        resultSet.getObject("goal_weight", Double.class),
                        resultSet.getObject("weekly_goal", Double.class),
                        ActivityLevel.valueOf(resultSet.getString("activity_level")),

                        resultSet.getInt("target_calories"),
                        resultSet.getDouble("target_carbs"),
                        resultSet.getDouble("target_fat"),
                        resultSet.getDouble("target_protein")
                ),
                userId
        );

        return results.stream().findFirst();
    }
}
