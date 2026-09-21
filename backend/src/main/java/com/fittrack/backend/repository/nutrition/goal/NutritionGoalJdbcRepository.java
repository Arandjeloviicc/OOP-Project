package com.fittrack.backend.repository.nutrition.goal;

import com.fittrack.backend.dto.nutrition.goal.NutritionTargets;
import com.fittrack.backend.dto.profile.editor.NutritionGoalUpdateRequest;
import com.fittrack.backend.entity.profile.ActivityLevel;
import com.fittrack.backend.entity.profile.Gender;
import com.fittrack.backend.entity.profile.WeightGoal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class NutritionGoalJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public NutritionGoalJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<NutritionTargets> findTargetsForDate(Integer userId, LocalDate date) {
        // user_goals - uzima sve nutrition goals korisnika
        // matched_goal - trazi goal koji vazi za izabrani datum na osnovu start_date i end_date
        // first_goal - uzima najstariji goal kada je izabrani datum stariji od prvog sacuvanog goal-a korisnika
        // UNION ALL - spaja matched_goal i first_goal, pri cemu se first_goal koristi samo ako matched_goal ne postoji
        // LIMIT 1 - garantuje da se vrati najvise jedan nutrition goal

        String sql = """
        WITH user_goals AS (
            SELECT
                target_calories,
                target_carbs,
                target_fat,
                target_protein,
                start_date,
                end_date
            FROM nutrition_goals
            WHERE user_id = ?
        ),
        matched_goal AS (
            SELECT
                target_calories,
                target_carbs,
                target_fat,
                target_protein
            FROM user_goals
            WHERE start_date <= ?
              AND (end_date IS NULL OR end_date >= ?)
            ORDER BY start_date DESC
            LIMIT 1
        ),
        first_goal AS (
            SELECT
                target_calories,
                target_carbs,
                target_fat,
                target_protein
            FROM user_goals
            WHERE ? < (
                SELECT MIN(start_date)
                FROM user_goals
            )
            ORDER BY start_date ASC
            LIMIT 1
        )
        SELECT *
        FROM matched_goal

        UNION ALL

        SELECT *
        FROM first_goal
        WHERE NOT EXISTS (
            SELECT 1
            FROM matched_goal
        )

        LIMIT 1
        """;

        return jdbcTemplate.query(
                sql,
                resultSet -> {
                    if (!resultSet.next()) {
                        return Optional.empty();
                    }

                    return Optional.of(
                            new NutritionTargets(
                                    resultSet.getInt("target_calories"),
                                    resultSet.getDouble("target_carbs"),
                                    resultSet.getDouble("target_fat"),
                                    resultSet.getDouble("target_protein")
                            )
                    );
                },
                userId,
                date,
                date,
                date
        );
    }

    public Optional<NutritionGoalRecalculationData> findRecalculationData(Integer userId) {
        // user_profiles - osnovni podaci korisnika potrebni za obracun (datum rodjenja, pol, visina)
        // nutrition_goals - uzima se samo aktivan cilj korisnika (end_date IS NULL)
        // current_weight (LATERAL JOIN) - uzima najnoviji unet unos tezine korisnika iz weight_logs, sortirano po logged_at pa po id-u da se razresi slucaj kada su dva unosa logovana u istom trenutku
        // LEFT JOIN - ako korisnik nema nijedan unos tezine, current_weight ostaje NULL umesto da se ceo red izbaci iz rezultata

        String sql = """
            SELECT
                ng.id AS goal_id,
    
                up.date_of_birth,
                up.gender,
                up.height,
    
                current_weight.weight AS current_weight,
    
                ng.activity_level,
                ng.goal_type,
                ng.goal_weight,
                ng.weekly_goal,
    
                ng.progress_start_weight,
                ng.progress_started_at,
    
                ng.start_date
    
            FROM user_profiles up
    
            JOIN nutrition_goals ng
                ON ng.user_id = up.user_id
               AND ng.end_date IS NULL
    
            LEFT JOIN LATERAL (
                SELECT wl.weight
                FROM weight_logs wl
                WHERE wl.user_id = up.user_id
                ORDER BY wl.logged_at DESC, wl.id DESC
                LIMIT 1
            ) current_weight ON TRUE
    
            WHERE up.user_id = ?
            """;

        List<NutritionGoalRecalculationData> results = jdbcTemplate.query(
                sql,
                (resultSet, _) -> new NutritionGoalRecalculationData(
                        resultSet.getInt("goal_id"),

                        resultSet.getObject(
                                "date_of_birth",
                                LocalDate.class
                        ),
                        Gender.valueOf(
                                resultSet.getString("gender")
                        ),
                        resultSet.getDouble("height"),

                        resultSet.getObject(
                                "current_weight",
                                Double.class
                        ),

                        ActivityLevel.valueOf(
                                resultSet.getString("activity_level")
                        ),
                        WeightGoal.valueOf(
                                resultSet.getString("goal_type")
                        ),
                        resultSet.getObject(
                                "goal_weight",
                                Double.class
                        ),
                        resultSet.getObject(
                                "weekly_goal",
                                Double.class
                        ),

                        resultSet.getObject(
                                "progress_start_weight",
                                Double.class
                        ),
                        resultSet.getTimestamp(
                                "progress_started_at"
                        ).toInstant(),

                        resultSet.getObject(
                                "start_date",
                                LocalDate.class
                        )
                ),
                userId
        );

        return results.stream().findFirst();
    }

    public int insertGoalVersion(Integer userId, NutritionGoalRecalculationData data, NutritionTargets targets, LocalDate startDate) {
        String sql = """
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
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;

        return jdbcTemplate.update(
                sql,
                userId,

                data.activityLevel().name(),
                data.goalType().name(),
                data.goalWeight(),
                data.weeklyGoal(),

                targets.calories(),
                targets.protein(),
                targets.carbs(),
                targets.fat(),

                data.progressStartWeight(),
                Timestamp.from(data.progressStartedAt()),

                startDate
        );
    }

    public int insertGoalVersion(Integer userId, NutritionGoalUpdateRequest request, NutritionTargets targets, LocalDate startDate, Double progressStartWeight, Instant progressStartedAt) {
        String sql = """
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
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;

        return jdbcTemplate.update(
                sql,
                userId,

                request.activityLevel().name(),
                request.goalType().name(),
                request.goalWeight(),
                request.weeklyGoal(),

                targets.calories(),
                targets.protein(),
                targets.carbs(),
                targets.fat(),

                progressStartWeight,
                Timestamp.from(progressStartedAt),

                startDate
        );
    }

    public int updateGoal(Integer goalId, NutritionGoalUpdateRequest request, NutritionTargets targets, Double progressStartWeight, Instant progressStartedAt) {
        String sql = """
            UPDATE nutrition_goals
            SET
                activity_level = ?,
                goal_type = ?,
                goal_weight = ?,
                weekly_goal = ?,
    
                target_calories = ?,
                target_protein = ?,
                target_carbs = ?,
                target_fat = ?,
    
                progress_start_weight = ?,
                progress_started_at = ?,
    
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
              AND end_date IS NULL
            """;

        return jdbcTemplate.update(
                sql,
                request.activityLevel().name(),
                request.goalType().name(),
                request.goalWeight(),
                request.weeklyGoal(),

                targets.calories(),
                targets.protein(),
                targets.carbs(),
                targets.fat(),

                progressStartWeight,
                Timestamp.from(progressStartedAt),

                goalId
        );
    }

    public int updateTargets(Integer goalId, NutritionTargets targets) {
        String sql = """
            UPDATE nutrition_goals
            SET
                target_calories = ?,
                target_protein = ?,
                target_carbs = ?,
                target_fat = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
              AND end_date IS NULL
            """;

        return jdbcTemplate.update(
                sql,
                targets.calories(),
                targets.protein(),
                targets.carbs(),
                targets.fat(),
                goalId
        );
    }

    public int closeGoal(Integer goalId, LocalDate endDate) {
        String sql = """
            UPDATE nutrition_goals
            SET
                end_date = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
              AND end_date IS NULL
            """;

        return jdbcTemplate.update(
                sql,
                endDate,
                goalId
        );
    }
}