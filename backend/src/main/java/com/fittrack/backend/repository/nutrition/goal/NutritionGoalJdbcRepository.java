package com.fittrack.backend.repository.nutrition.goal;

import com.fittrack.backend.dto.nutrition.goal.NutritionTargets;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
}