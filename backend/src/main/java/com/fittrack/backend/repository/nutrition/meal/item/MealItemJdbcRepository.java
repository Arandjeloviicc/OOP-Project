package com.fittrack.backend.repository.nutrition.meal.item;

import com.fittrack.backend.dto.nutrition.meal.item.CreateMealItemRequest;
import com.fittrack.backend.dto.nutrition.meal.item.MealItemResponse;
import com.fittrack.backend.entity.nutrition.MealItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
public class MealItemJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public MealItemJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ── Bulk Meal Item Operations ─────────────────────────────────────
    public void insertAll(Integer mealId, List<MealItem> items) {
        if (items.isEmpty()) {
            return;
        }

        String values = repeatPlaceholder(
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                items.size()
        );

        String sql = """
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
                VALUES
                """ + values;

        List<Object> parameters = buildParameters(mealId, items);

        jdbcTemplate.update(
                sql,
                parameters.toArray()
        );
    }

    public void updateQuantities(Integer mealId, Map<Integer, Double> quantitiesByItemId) {
        if (quantitiesByItemId.isEmpty()) {
            return;
        }

        String values = String.join(
                ", ",
                Collections.nCopies(
                        quantitiesByItemId.size(),
                        "(?, ?)"
                )
        );

        String sql = """
            UPDATE meal_items AS mi
            SET quantity_grams = v.quantity_grams,
                updated_at = CURRENT_TIMESTAMP
            FROM (VALUES
            """ + values + """
            ) AS v(id, quantity_grams)
            WHERE mi.id = v.id
              AND mi.meal_id = ?
            """;

        List<Object> parameters = new ArrayList<>();

        for (Map.Entry<Integer, Double> entry
                : quantitiesByItemId.entrySet()) {
            parameters.add(entry.getKey());
            parameters.add(entry.getValue());
        }

        parameters.add(mealId);

        jdbcTemplate.update(
                sql,
                parameters.toArray()
        );
    }

    public void deleteByIds(Integer mealId, List<Integer> mealItemIds) {
        if (mealItemIds.isEmpty()) {
            return;
        }

        String placeholders = String.join(
                ", ",
                Collections.nCopies(
                        mealItemIds.size(),
                        "?"
                )
        );

        String sql = """
            DELETE FROM meal_items
            WHERE meal_id = ?
              AND id IN (
            """ + placeholders + ")";

        List<Object> parameters = new ArrayList<>();

        parameters.add(mealId);
        parameters.addAll(mealItemIds);

        jdbcTemplate.update(
                sql,
                parameters.toArray()
        );
    }

    public void updateAndDelete(Integer mealId, Map<Integer, Double> quantitiesByItemId, List<Integer> mealItemIdsToDelete) {
        boolean hasUpdates = !quantitiesByItemId.isEmpty();
        boolean hasDeletes = !mealItemIdsToDelete.isEmpty();

        // No update nor delete
        if (!hasUpdates && !hasDeletes) {
            return;
        }

        // Just Delete
        if (!hasUpdates) {
            deleteByIds(mealId, mealItemIdsToDelete);
            return;
        }

        // Just Update
        if (!hasDeletes) {
            updateQuantities(mealId, quantitiesByItemId);
            return;
        }

        // Both Delete and Update

        // Spajamo ih u jedan sql statement kako bismo izbegli dva odvojena round-trip-a do baze
        String sql = buildUpdateAndDeleteSql(quantitiesByItemId.size(), mealItemIdsToDelete.size());

        List<Object> parameters = buildUpdateAndDeleteParams(mealId, quantitiesByItemId, mealItemIdsToDelete);

        jdbcTemplate.update(
                sql,
                parameters.toArray()
        );
    }

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
            throw new IllegalArgumentException("User or food not found.");
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
                (resultSet, rowNum) -> new MealItemResponse(
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

    public void copyDailyMeal(Integer userId, LocalDate sourceDate, String sourceMealName, LocalDate targetDate, String targetMealName) {
        // source_meal - nalazi meal koji se menja
        // existing_target - proverava da li cilji obrok postoji vec u bazi
        // inserted_target - ako ciljni obrok ne postoji, kreira se i vraca se njegov id
        // target_meal - spaja existing i inserted jer je jedan uvek prazan
        // INSERT - kopira sve iteme iz izvornog obroka u ciljni obrok koristeci vrednosti iz source_meal da ne bi ponovo trazio po id, ako je source meal prazan, nista se ne kopira

        String sql = """
            WITH source_meal AS (
                SELECT
                    m.id,
                    m.user_id
                FROM meals m
                WHERE m.user_id = ?
                  AND m.meal_date = ?
                  AND m.name = ?
                  AND m.kind = 'DAILY'
            ),
            existing_target AS (
                SELECT m.id
                FROM meals m
                JOIN source_meal sm
                    ON sm.user_id = m.user_id
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
                    sm.user_id,
                    ?,
                    ?,
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
                mi.food_id,
                mi.quantity_grams,
                mi.food_name,
                mi.brand,
                mi.serving_size_grams,
                mi.calories_per_serving,
                mi.protein_per_serving,
                mi.carbs_per_serving,
                mi.fat_per_serving,
                CURRENT_TIMESTAMP
            FROM meal_items mi
            JOIN source_meal sm
                ON sm.id = mi.meal_id
            CROSS JOIN target_meal tm
            """;

        int copiedItems = jdbcTemplate.update(
                sql,
                userId,
                sourceDate,
                sourceMealName,
                targetDate,
                targetMealName,
                targetMealName,
                targetDate
        );

        if (copiedItems == 0) {
            throw new IllegalArgumentException("Source meal not found or has no items.");
        }
    }

    // ── Saved Meal Operations ───────────────────────────────────────────────────────
    public void createSavedMeal(Integer userId, String mealName, List<CreateMealItemRequest> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Saved meal must contain at least one item.");
        }

        String itemValues = repeatPlaceholder(
                "(?, ?, ?, ?, ?, ?, ?, ?, ?)",
                items.size()
        );

        // input_items - pretvara sve iteme iz requesta u privremenu tabelu
        // valid_user - proverava da korisnik postoji
        // valid_items - proverava svaki food_id; null food_id je dozvoljen za custom item
        // inserted_meal - kreira SAVED meal samo ako su svi itemi validni
        // inserted_items - ubacuje sve iteme odjednom u novokreirani meal
        // SELECT - vraca rezultat validacije bez dodatnog odlaska do baze
        String sql = """
            WITH input_items (
                food_id,
                food_name,
                brand,
                quantity_grams,
                serving_size_grams,
                calories_per_serving,
                protein_per_serving,
                carbs_per_serving,
                fat_per_serving
            ) AS (
                VALUES %s
            ),
            valid_user AS (
                SELECT id
                FROM users
                WHERE id = ?
            ),
            valid_items AS (
                SELECT i.*
                FROM input_items i
                CROSS JOIN valid_user vu
                LEFT JOIN foods f
                    ON f.id = i.food_id
                WHERE i.food_id IS NULL
                   OR f.id IS NOT NULL
            ),
            inserted_meal AS (
                INSERT INTO meals (
                    user_id,
                    name,
                    meal_date,
                    kind,
                    created_at
                )
                SELECT
                    vu.id,
                    ?,
                    NULL,
                    'SAVED',
                    CURRENT_TIMESTAMP
                FROM valid_user vu
                WHERE (
                    SELECT COUNT(*)
                    FROM valid_items
                ) = (
                    SELECT COUNT(*)
                    FROM input_items
                )
                RETURNING id
            ),
            inserted_items AS (
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
                    im.id,
                    vi.food_id,
                    vi.quantity_grams,
                    vi.food_name,
                    vi.brand,
                    vi.serving_size_grams,
                    vi.calories_per_serving,
                    vi.protein_per_serving,
                    vi.carbs_per_serving,
                    vi.fat_per_serving,
                    CURRENT_TIMESTAMP
                FROM valid_items vi
                CROSS JOIN inserted_meal im
                RETURNING id
            )
            SELECT
                EXISTS (
                    SELECT 1
                    FROM valid_user
                ) AS user_exists,
                (
                    SELECT COUNT(*)
                    FROM input_items
                ) AS requested_count,
                (
                    SELECT COUNT(*)
                    FROM valid_items
                ) AS valid_count,
                (
                    SELECT COUNT(*)
                    FROM inserted_items
                ) AS inserted_count
            """.formatted(itemValues);

        List<Object> parameters = buildCreateSavedMealParams(
                userId,
                mealName,
                items
        );

        CreateSavedMealResult result = jdbcTemplate.queryForObject(
                sql,
                (resultSet, rowNum) ->
                        new CreateSavedMealResult(
                                resultSet.getBoolean("user_exists"),
                                resultSet.getInt("requested_count"),
                                resultSet.getInt("valid_count"),
                                resultSet.getInt("inserted_count")
                        ),
                parameters.toArray()
        );

        if (result == null || !result.userExists()) {
            throw new IllegalArgumentException("User not found.");
        }

        if (result.validCount() != result.requestedCount()) {
            throw new IllegalArgumentException("Food not found.");
        }

        if (result.insertedCount() != result.requestedCount()) {
            throw new IllegalStateException("Failed to create saved meal.");
        }
    }

    public void logSavedMeal(Integer userId, Integer savedMealId, LocalDate targetDate, String targetMealName) {
        // source_meal - nalazi saved meal koji se dodaje u daily meal
        // existing_target - proverava da li cilji daily obrok postoji vec u bazi
        // inserted_target - ako ciljni daily obrok ne postoji, kreira se i vraca se njegov id
        // target_meal - spaja existing i inserted jer je jedan uvek prazan
        // INSERT - kopira sve iteme iz saved obroka u ciljni daily obrok koristeci vrednosti iz source_meal da ne bi ponovo trazio po id, ako je source meal prazan, nista se ne kopira

        String sql = """
            WITH source_meal AS (
                SELECT
                    m.id,
                    m.user_id
                FROM meals m
                WHERE m.id = ?
                  AND m.user_id = ?
                  AND m.kind = 'SAVED'
            ),
            existing_target AS (
                SELECT m.id
                FROM meals m
                JOIN source_meal sm
                    ON sm.user_id = m.user_id
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
                    sm.user_id,
                    ?,
                    ?,
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
                mi.food_id,
                mi.quantity_grams,
                mi.food_name,
                mi.brand,
                mi.serving_size_grams,
                mi.calories_per_serving,
                mi.protein_per_serving,
                mi.carbs_per_serving,
                mi.fat_per_serving,
                CURRENT_TIMESTAMP
            FROM meal_items mi
            JOIN source_meal sm
                ON sm.id = mi.meal_id
            CROSS JOIN target_meal tm
            """;

        int inserted = jdbcTemplate.update(
                sql,
                savedMealId,
                userId,
                targetDate,
                targetMealName,
                targetMealName,
                targetDate
        );

        if (inserted == 0) {
            throw new IllegalArgumentException("Saved meal not found or has no items.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private static List<Object> buildParameters(Integer mealId, List<MealItem> items) {
        List<Object> parameters = new ArrayList<>();

        for (MealItem item : items) {
            parameters.add(mealId);

            parameters.add(
                    item.getFood() != null
                            ? item.getFood().getId()
                            : null
            );

            parameters.add(item.getQuantityGrams());
            parameters.add(item.getFoodName());
            parameters.add(item.getBrand());
            parameters.add(item.getServingSizeGrams());
            parameters.add(item.getCaloriesPerServing());
            parameters.add(item.getProteinPerServing());
            parameters.add(item.getCarbsPerServing());
            parameters.add(item.getFatPerServing());
        }
        return parameters;
    }

    // Pravi dinamicki SQL sa tacnim brojem placeholder-a
    // UPDATE ide u CTE, a DELETE je glavni statement pa se obe operacije izvrsavaju u jednom pozivu baze
    private String buildUpdateAndDeleteSql(int updateCount, int deleteCount) {
        String valuesPlaceholder = repeatPlaceholder("(?, ?)", updateCount);
        String idsPlaceholder = repeatPlaceholder("?", deleteCount);

        return """
            WITH updated_items AS (
                UPDATE meal_items AS mi
                SET quantity_grams = v.quantity_grams,
                    updated_at = CURRENT_TIMESTAMP
                FROM (VALUES %s) AS v(id, quantity_grams)
                WHERE mi.id = v.id AND mi.meal_id = ?
                RETURNING mi.id
            )
            DELETE FROM meal_items
            WHERE meal_id = ? AND id IN (%s)
            """.formatted(valuesPlaceholder, idsPlaceholder);
    }

    private String repeatPlaceholder(String piece, int count) {
        return String.join(", ", Collections.nCopies(count, piece));
    }

    // Parametri moraju biti dodati istim redosledom kojim se
    // pojavljuju "?" placeholder-i u generisanom SQL-u
    private List<Object> buildUpdateAndDeleteParams(Integer mealId, Map<Integer, Double> quantitiesByItemId, List<Integer> mealItemIdsToDelete) {
        List<Object> parameters = new ArrayList<>();

        // Parametri za VALUES (?, ?), (?, ?), ...
        for (Map.Entry<Integer, Double> entry : quantitiesByItemId.entrySet()) {
            parameters.add(entry.getKey());
            parameters.add(entry.getValue());
        }

        // meal_id za UPDATE
        parameters.add(mealId);

        // meal_id i ID-jevi za DELETE
        parameters.add(mealId);
        parameters.addAll(mealItemIdsToDelete);

        return parameters;
    }

    private List<Object> buildCreateSavedMealParams(Integer userId, String mealName, List<CreateMealItemRequest> items) {
        List<Object> parameters = new ArrayList<>();

        // Parametri za input_items VALUES (...), (...), ...
        for (CreateMealItemRequest item : items) {
            parameters.add(item.foodId());
            parameters.add(item.foodName().trim());
            parameters.add(item.brand());
            parameters.add(item.quantityGrams());
            parameters.add(item.servingSizeGrams());
            parameters.add(item.caloriesPerServing());
            parameters.add(item.proteinPerServing());
            parameters.add(item.carbsPerServing());
            parameters.add(item.fatPerServing());
        }

        // valid_user
        parameters.add(userId);

        // inserted_meal
        parameters.add(mealName.trim());

        return parameters;
    }

    // ── Record ───────────────────────────────────────────────────────
    private record CreateSavedMealResult(
            boolean userExists,
            int requestedCount,
            int validCount,
            int insertedCount
    ) {}
}

