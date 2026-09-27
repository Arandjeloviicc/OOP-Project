package com.fittrack.backend.repository.nutrition.meal.item;

import com.fittrack.backend.dto.nutrition.meal.item.MealItemResponse;
import com.fittrack.backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class MealItemJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    // ── Daily Meal Item Operations ────────────────────────────────────
    public void addDailyItem(Integer userId, LocalDate mealDate, String mealName, Integer foodId, double quantityGrams) {
        // valid_context - proverava da korisnik postoji i da hrana postoji, kao i kome hrana pripada
        // existing_target - proverava da li cilji obrok postoji vec u bazi
        // inserted_target - ako ciljni obrok ne postoji, kreira se i vraca se njegov id
        // target_meal - spaja existing i inserted jer je jedan uvek prazan
        // INSERT - dodaje novu stavku u meal_items, koristi podatke iz valid_context da ne bi morao ponovo da trazi po food_id
        // CROSS JOIN - ako korisnik/hrana nisu validni, valid_context je prazan, pa spajanje sa praznim rezultatom daje 0 redova i INSERT nista ne unosi

        String sql = """
                WITH valid_context AS (
                    SELECT
                        u.id AS user_id,
                        f.id AS food_id,
                        f.name AS food_name,
                        f.brand,
                        f.serving_size_grams,
                        f.calories_per_serving,
                        f.protein_per_serving,
                        f.carbs_per_serving,
                        f.fat_per_serving
                    FROM users u
                    JOIN foods f
                       ON f.id = ?
                    WHERE u.id = ?
                ),
                existing_target AS (
                    SELECT m.id
                    FROM meals m
                    JOIN valid_context vc
                        ON vc.user_id = m.user_id
                    WHERE m.meal_date = ?
                      AND m.name = ?
                      AND m.kind = 'DAILY'
                    ORDER BY m.id
                    LIMIT 1
                ),
                 inserted_target AS (
                    INSERT INTO meals (
                        user_id,
                        name,
                        meal_date,
                        kind,
                        created_at
                    )
                    SELECT
                        vc.user_id,
                        ?,
                        ?,
                        'DAILY',
                        CURRENT_TIMESTAMP
                    FROM valid_context vc
                    WHERE NOT EXISTS (
                        SELECT 1
                        FROM existing_target
                    )
                    RETURNING id
                ),
                target_meal AS (
                    SELECT id
                    FROM existing_target
    
                    UNION ALL
    
                    SELECT id
                    FROM inserted_target
    
                    LIMIT 1
                )
                INSERT INTO meal_items (
                    meal_id,
                    food_id,
                    quantity_grams,
                    food_name,
                    brand,
                    serving_size_grams,
                    calories_per_serving,
                    protein_per_serving,
                    carbs_per_serving,
                    fat_per_serving,
                    created_at
                )
                SELECT
                    tm.id,
                    vc.food_id,
                    ?,
                    vc.food_name,
                    vc.brand,
                    vc.serving_size_grams,
                    vc.calories_per_serving,
                    vc.protein_per_serving,
                    vc.carbs_per_serving,
                    vc.fat_per_serving,
                    CURRENT_TIMESTAMP
                FROM target_meal tm
                CROSS JOIN valid_context vc
                """;

        int inserted = jdbcTemplate.update(
                sql,
                foodId,
                userId,
                mealDate,
                mealName,
                mealName,
                mealDate,
                quantityGrams
        );

        if (inserted == 0) {
            throw new ResourceNotFoundException("User or food not found.");
        }
    }

    public Optional<MealItemResponse> updateDailyItem(Integer userId, Integer mealItemId, String targetMealName, double quantityGrams) {
        // source_meal - nalazi meal koji se menja
        // existing_target - proverava da li cilji obrok postoji vec u bazi
        // inserted_target - ako ciljni obrok ne postoji, kreira se i vraca se njegov id
        // target_meal - spaja existing i inserted jer je jedan uvek prazan
        // UPDATE - azurira stavku
        // RETURNING - odamh vraca azurirane vrednosti bez dodatnog selecta (Specificno za PostgreSQL)

        String sql = """
            WITH source_meal AS (
                SELECT
                    m.user_id,
                    m.meal_date
                FROM meal_items mi
                JOIN meals m
                    ON m.id = mi.meal_id
                WHERE mi.id = ?
                  AND m.user_id = ?
                  AND m.kind = 'DAILY'
            ),
            existing_target AS (
                SELECT m.id
                FROM meals m
                JOIN source_meal sm
                    ON sm.user_id = m.user_id
                   AND sm.meal_date = m.meal_date
                WHERE m.kind = 'DAILY'
                  AND m.name = ?
                ORDER BY m.id
                LIMIT 1
            ),
            inserted_target AS (
                INSERT INTO meals (
                    user_id,
                    name,
                    meal_date,
                    kind,
                    created_at
                )
                SELECT
                    sm.user_id,
                    ?,
                    sm.meal_date,
                    'DAILY',
                    CURRENT_TIMESTAMP
                FROM source_meal sm
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM existing_target
                )
                RETURNING id
            ),
            target_meal AS (
                SELECT id
                FROM existing_target

                UNION ALL

                SELECT id
                FROM inserted_target

                LIMIT 1
            )
            UPDATE meal_items mi
            SET
                quantity_grams = ?,
                meal_id = tm.id,
                updated_at = CURRENT_TIMESTAMP
            FROM target_meal tm
            WHERE mi.id = ?
            RETURNING
                mi.id,
                mi.food_id,
                mi.food_name,
                mi.brand,
                mi.quantity_grams,
                mi.serving_size_grams,
                mi.calories_per_serving,
                mi.protein_per_serving,
                mi.carbs_per_serving,
                mi.fat_per_serving
            """;

        List<MealItemResponse> results = jdbcTemplate.query(
                sql,
                (resultSet, _) -> new MealItemResponse(
                        resultSet.getInt("id"),
                        resultSet.getObject(
                                "food_id",
                                Integer.class
                        ),
                        resultSet.getString("food_name"),
                        resultSet.getString("brand"),
                        resultSet.getDouble("quantity_grams"),
                        resultSet.getDouble("serving_size_grams"),
                        resultSet.getDouble("calories_per_serving"),
                        resultSet.getDouble("protein_per_serving"),
                        resultSet.getDouble("carbs_per_serving"),
                        resultSet.getDouble("fat_per_serving")
                ),
                mealItemId,
                userId,
                targetMealName,
                targetMealName,
                quantityGrams,
                mealItemId
        );

        return results.stream().findFirst();
    }

    public int deleteDailyItem(Integer userId, Integer mealItemId) {
        String sql = """
            DELETE FROM meal_items mi
            USING meals m
            WHERE mi.id = ?
              AND mi.meal_id = m.id
              AND m.user_id = ?
              AND m.kind = 'DAILY'
            """;

        return jdbcTemplate.update(
                sql,
                mealItemId,
                userId
        );
    }
}