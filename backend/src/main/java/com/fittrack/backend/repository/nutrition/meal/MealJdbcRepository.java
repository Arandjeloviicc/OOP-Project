package com.fittrack.backend.repository.nutrition.meal;

import com.fittrack.backend.dto.nutrition.meal.MealResponse;
import com.fittrack.backend.dto.nutrition.meal.UpdateSavedMealRequest;
import com.fittrack.backend.dto.nutrition.meal.item.CreateMealItemRequest;
import com.fittrack.backend.dto.nutrition.meal.item.MealItemResponse;
import com.fittrack.backend.dto.nutrition.meal.item.UpdateSavedMealItemRequest;
import com.fittrack.backend.domain.nutrition.MealKind;
import com.fittrack.backend.exception.ResourceNotFoundException;
import com.fittrack.backend.repository.nutrition.meal.item.projection.CreateSavedMealResult;
import com.fittrack.backend.repository.nutrition.meal.projection.UpdateSavedMealResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MealJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    // ── Read ───────────────────────────────────────────────────────
    public List<MealResponse> findByUserIdAndMealDate(Integer userId, LocalDate mealDate, MealKind kind) {
        String sql = """
            SELECT
                m.id AS meal_id,
                m.name AS meal_name,
                m.meal_date,
                mi.id AS item_id,
                mi.food_id,
                mi.food_name,
                mi.brand,
                mi.quantity_grams,
                mi.serving_size_grams,
                mi.calories_per_serving,
                mi.protein_per_serving,
                mi.carbs_per_serving,
                mi.fat_per_serving
            FROM meals m
            LEFT JOIN meal_items mi
                ON mi.meal_id = m.id
            WHERE m.user_id = ?
              AND m.meal_date = ?
              AND m.kind = ?
            ORDER BY m.id, mi.id
            """;

        return jdbcTemplate.query(
                sql,
                this::mapMeals,
                userId,
                mealDate,
                kind.name()
        );
    }

    public List<MealResponse> findByUserIdAndKind(Integer userId, MealKind kind) {
        String sql = """
            SELECT
                m.id AS meal_id,
                m.name AS meal_name,
                m.meal_date,
                mi.id AS item_id,
                mi.food_id,
                mi.food_name,
                mi.brand,
                mi.quantity_grams,
                mi.serving_size_grams,
                mi.calories_per_serving,
                mi.protein_per_serving,
                mi.carbs_per_serving,
                mi.fat_per_serving
            FROM meals m
            LEFT JOIN meal_items mi
                ON mi.meal_id = m.id
            WHERE m.user_id = ?
              AND m.kind = ?
            ORDER BY m.name, m.id, mi.id
            """;

        return jdbcTemplate.query(
                sql,
                this::mapMeals,
                userId,
                kind.name()
        );
    }

    public List<MealResponse> findByUserIdAndKindAndNameContaining(Integer userId, MealKind kind, String search) {
        String sql = """
            SELECT
                m.id AS meal_id,
                m.name AS meal_name,
                m.meal_date,
                mi.id AS item_id,
                mi.food_id,
                mi.food_name,
                mi.brand,
                mi.quantity_grams,
                mi.serving_size_grams,
                mi.calories_per_serving,
                mi.protein_per_serving,
                mi.carbs_per_serving,
                mi.fat_per_serving
            FROM meals m
            LEFT JOIN meal_items mi
                ON mi.meal_id = m.id
            WHERE m.user_id = ?
              AND m.kind = ?
              AND LOWER(m.name) LIKE LOWER(CONCAT('%', ?, '%'))
            ORDER BY m.name, m.id, mi.id
            """;

        return jdbcTemplate.query(
                sql,
                this::mapMeals,
                userId,
                kind.name(),
                search
        );
    }

    private List<MealResponse> mapMeals(ResultSet resultSet) throws SQLException {
        List<MealResponse> meals = new ArrayList<>();

        Integer currentMealId = null;
        String currentMealName = null;
        LocalDate currentMealDate = null;
        List<MealItemResponse> currentItems = null;

        while (resultSet.next()) {
            Integer mealId = resultSet.getInt("meal_id");

            if (currentMealId == null || !currentMealId.equals(mealId)) {
                if (currentMealId != null) {
                    meals.add(
                            new MealResponse(
                                    currentMealId,
                                    currentMealName,
                                    currentMealDate,
                                    currentItems
                            )
                    );
                }

                currentMealId = mealId;
                currentMealName = resultSet.getString("meal_name");
                currentMealDate = resultSet.getObject(
                        "meal_date",
                        LocalDate.class
                );

                currentItems = new ArrayList<>();
            }

            Integer itemId = resultSet.getObject(
                    "item_id",
                    Integer.class
            );

            if (itemId != null) {
                currentItems.add(
                        new MealItemResponse(
                                itemId,
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
                        )
                );
            }
        }

        if (currentMealId != null) {
            meals.add(
                    new MealResponse(
                            currentMealId,
                            currentMealName,
                            currentMealDate,
                            currentItems
                    )
            );
        }

        return meals;
    }

    // ── Create ───────────────────────────────────────────────────────
    public void createSavedMeal(Integer userId, String mealName, List<CreateMealItemRequest> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Saved meal must contain at least one item.");
        }

        String itemValues = String.join(
                ", ",
                Collections.nCopies(
                        items.size(),
                        "(?, ?, ?, ?, ?, ?, ?, ?, ?)")
        );

        // input_items - pretvara sve iteme iz requesta u privremenu tabelu
        // valid_user - proverava da korisnik postoji
        // valid_items - proverava svaki food_id (null food_id je dozvoljen za custom item) i, kada food_id postoji, zamenjuje sve nutritivne vrednosti iz requesta autoritativnim vrednostima iz foods tabele, da klijent ne bi mogao da posalje proizvoljne kalorije/makronutrijente za postojecu hranu
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
                  SELECT
                      i.food_id,
            
                      CASE
                          WHEN i.food_id IS NULL THEN i.food_name
                          ELSE f.name
                      END AS food_name,
            
                      CASE
                          WHEN i.food_id IS NULL THEN i.brand
                          ELSE f.brand
                      END AS brand,
            
                      i.quantity_grams,
            
                      CASE
                          WHEN i.food_id IS NULL THEN i.serving_size_grams
                          ELSE f.serving_size_grams
                      END AS serving_size_grams,
            
                      CASE
                          WHEN i.food_id IS NULL THEN i.calories_per_serving
                          ELSE f.calories_per_serving
                      END AS calories_per_serving,
            
                      CASE
                          WHEN i.food_id IS NULL THEN i.protein_per_serving
                          ELSE f.protein_per_serving
                      END AS protein_per_serving,
            
                      CASE
                          WHEN i.food_id IS NULL THEN i.carbs_per_serving
                          ELSE f.carbs_per_serving
                      END AS carbs_per_serving,
            
                      CASE
                          WHEN i.food_id IS NULL THEN i.fat_per_serving
                          ELSE f.fat_per_serving
                      END AS fat_per_serving
            
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
                (resultSet, _) ->
                        new CreateSavedMealResult(
                                resultSet.getBoolean("user_exists"),
                                resultSet.getInt("requested_count"),
                                resultSet.getInt("valid_count"),
                                resultSet.getInt("inserted_count")
                        ),
                parameters.toArray()
        );

        if (result == null || !result.userExists()) {
            throw new ResourceNotFoundException("User not found.");
        }

        if (result.validCount() != result.requestedCount()) {
            throw new ResourceNotFoundException("Food not found.");
        }

        if (result.insertedCount() != result.requestedCount()) {
            throw new IllegalStateException("Failed to create saved meal.");
        }
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

    // ── Update ───────────────────────────────────────────────────────
    public void updateSavedMeal(Integer userId, Integer mealId, UpdateSavedMealRequest request) {
        if (request.items().isEmpty()) {
            throw new IllegalArgumentException(
                    "Saved meal must contain at least one item."
            );
        }

        String itemValues = String.join(
                ", ",
                Collections.nCopies(
                        request.items().size(),
                        "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                )
        );

        // input_items - pretvara sve iteme iz requesta u privremenu tabelu
        // target_meal - nalazi meal po prosledjenom id-u
        // saved_meal - proverava da li je pronadjeni meal SAVED
        // valid_meal - proverava da li meal pripada korisniku
        // valid_existing_items - proverava da li svi postojeci itemi iz requesta pripadaju tom meal-u
        // valid_new_items - proverava nove iteme i za postojece food_id uzima podatke iz foods tabele
        // validation - sabira rezultate svih provera
        // can_update - dozvoljava izmene samo ako su sve prethodne provere prosle
        // updated_meal - menja naziv meal-a
        // updated_items - menja kolicinu postojecih itema kojima je quantity promenjen
        // deleted_items - brise iteme koji vise nisu poslati u requestu
        // inserted_items - ubacuje nove iteme u meal
        // SELECT - vraca rezultat validacije i broj izvrsenih izmena bez dodatnog odlaska do baze

        String sql = """
            WITH input_items (
                meal_item_id,
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
            target_meal AS (
                SELECT
                    id,
                    user_id,
                    kind
                FROM meals
                WHERE id = ?
            ),
            saved_meal AS (
                SELECT
                    id,
                    user_id
                FROM target_meal
                WHERE kind = 'SAVED'
            ),
            valid_meal AS (
                SELECT id
                FROM saved_meal
                WHERE user_id = ?
            ),
            valid_existing_items AS (
                SELECT i.meal_item_id
                FROM input_items i
                JOIN meal_items mi
                    ON mi.id = i.meal_item_id
                JOIN valid_meal vm
                    ON vm.id = mi.meal_id
                WHERE i.meal_item_id IS NOT NULL
            ),
            valid_new_items AS (
                SELECT
                    i.food_id,
    
                    CASE
                        WHEN i.food_id IS NULL THEN i.food_name
                        ELSE f.name
                    END AS food_name,
    
                    CASE
                        WHEN i.food_id IS NULL THEN i.brand
                        ELSE f.brand
                    END AS brand,
    
                    i.quantity_grams,
    
                    CASE
                        WHEN i.food_id IS NULL THEN i.serving_size_grams
                        ELSE f.serving_size_grams
                    END AS serving_size_grams,
    
                    CASE
                        WHEN i.food_id IS NULL THEN i.calories_per_serving
                        ELSE f.calories_per_serving
                    END AS calories_per_serving,
    
                    CASE
                        WHEN i.food_id IS NULL THEN i.protein_per_serving
                        ELSE f.protein_per_serving
                    END AS protein_per_serving,
    
                    CASE
                        WHEN i.food_id IS NULL THEN i.carbs_per_serving
                        ELSE f.carbs_per_serving
                    END AS carbs_per_serving,
    
                    CASE
                        WHEN i.food_id IS NULL THEN i.fat_per_serving
                        ELSE f.fat_per_serving
                    END AS fat_per_serving
    
                FROM input_items i
                CROSS JOIN valid_meal vm
    
                LEFT JOIN foods f
                    ON f.id = i.food_id
    
                WHERE i.meal_item_id IS NULL
                  AND (
                      i.food_id IS NULL
                      OR f.id IS NOT NULL
                  )
            ),
            validation AS (
                SELECT
                    EXISTS (
                        SELECT 1
                        FROM target_meal
                    ) AS meal_exists,
            
                    EXISTS (
                        SELECT 1
                        FROM saved_meal
                    ) AS saved_meal,
            
                    EXISTS (
                        SELECT 1
                        FROM valid_meal
                    ) AS belongs_to_user,
            
                    (
                        SELECT COUNT(*)
                        FROM input_items
                        WHERE meal_item_id IS NOT NULL
                    ) AS requested_existing_count,
            
                    (
                        SELECT COUNT(*)
                        FROM valid_existing_items
                    ) AS valid_existing_count,
            
                    (
                        SELECT COUNT(*)
                        FROM input_items
                        WHERE meal_item_id IS NULL
                    ) AS requested_new_count,
            
                    (
                        SELECT COUNT(*)
                        FROM valid_new_items
                    ) AS valid_new_count
            ),
            can_update AS (
                SELECT 1
                FROM validation
                WHERE meal_exists = TRUE
                  AND saved_meal = TRUE
                  AND belongs_to_user = TRUE
                  AND requested_existing_count = valid_existing_count
                  AND requested_new_count = valid_new_count
            ),
            updated_meal AS (
                UPDATE meals m
                SET
                    name = ?,
                    updated_at = CURRENT_TIMESTAMP
                FROM valid_meal vm
                CROSS JOIN can_update cu
                WHERE m.id = vm.id
                RETURNING m.id
            ),
            updated_items AS (
                UPDATE meal_items mi
                SET
                  quantity_grams = i.quantity_grams,
                  updated_at = CURRENT_TIMESTAMP
                FROM input_items i,
                   updated_meal um
                WHERE mi.id = i.meal_item_id
                  AND mi.meal_id = um.id
                  AND i.meal_item_id IS NOT NULL
                  AND mi.quantity_grams IS DISTINCT FROM i.quantity_grams
                RETURNING mi.id
            ),
            deleted_items AS (
                DELETE FROM meal_items mi
                USING updated_meal um
                WHERE mi.meal_id = um.id
                  AND NOT EXISTS (
                      SELECT 1
                      FROM input_items i
                      WHERE i.meal_item_id = mi.id
                  )
                RETURNING mi.id
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
                    um.id,
                    vni.food_id,
                    vni.quantity_grams,
                    vni.food_name,
                    vni.brand,
                    vni.serving_size_grams,
                    vni.calories_per_serving,
                    vni.protein_per_serving,
                    vni.carbs_per_serving,
                    vni.fat_per_serving,
                    CURRENT_TIMESTAMP
                FROM valid_new_items vni
                CROSS JOIN updated_meal um
                RETURNING id
            )
            SELECT
                v.meal_exists,
                v.saved_meal,
                v.belongs_to_user,
                v.requested_existing_count,
                v.valid_existing_count,
                v.requested_new_count,
                v.valid_new_count,
                (
                    SELECT COUNT(*)
                    FROM updated_meal
                ) AS updated_meal_count,
                (
                    SELECT COUNT(*)
                    FROM inserted_items
                ) AS inserted_count
            FROM validation v
            """.formatted(itemValues);

        List<Object> parameters = buildUpdateSavedMealParameters(userId, mealId, request);

        UpdateSavedMealResult result = jdbcTemplate.queryForObject(
                sql,
                (resultSet, _) ->
                        new UpdateSavedMealResult(
                                resultSet.getBoolean("meal_exists"),
                                resultSet.getBoolean("saved_meal"),
                                resultSet.getBoolean("belongs_to_user"),
                                resultSet.getInt("requested_existing_count"),
                                resultSet.getInt("valid_existing_count"),
                                resultSet.getInt("requested_new_count"),
                                resultSet.getInt("valid_new_count"),
                                resultSet.getInt("updated_meal_count"),
                                resultSet.getInt("inserted_count")
                        ),
                parameters.toArray()
        );

        if (result == null || !result.mealExists()) {
            throw new ResourceNotFoundException("Meal not found.");
        }

        if (!result.savedMeal()) {
            throw new IllegalArgumentException("Meal is not a saved meal.");
        }

        if (!result.belongsToUser()) {
            throw new IllegalArgumentException("Meal does not belong to user.");
        }

        if (result.validExistingCount() != result.requestedExistingCount()) {
            throw new IllegalArgumentException("Meal item does not belong to meal.");
        }

        if (result.validNewCount() != result.requestedNewCount()) {
            throw new ResourceNotFoundException("Food not found.");
        }

        if (result.updatedMealCount() != 1) {
            throw new IllegalStateException("Failed to update saved meal.");
        }

        if (result.insertedCount() != result.requestedNewCount()) {
            throw new IllegalStateException("Failed to insert saved meal items.");
        }
    }

    private static List<Object> buildUpdateSavedMealParameters(Integer userId, Integer mealId, UpdateSavedMealRequest request) {
        List<Object> parameters = new ArrayList<>();

        for (UpdateSavedMealItemRequest item : request.items()) {
            parameters.add(item.mealItemId());
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

        // target_meal
        parameters.add(mealId);

        // valid_meal
        parameters.add(userId);

        // updated_meal
        parameters.add(request.name().trim());

        return parameters;
    }

    // ── Delete ───────────────────────────────────────────────────────
    public int deleteByIdAndUserIdAndKind(Integer mealId, Integer userId, MealKind kind) {
        String sql = """
            DELETE FROM meals
            WHERE id = ?
              AND user_id = ?
              AND kind = ?
            """;

        return jdbcTemplate.update(
                sql,
                mealId,
                userId,
                kind.name()
        );
    }

    // ── Log ───────────────────────────────────────────────────────
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
            throw new ResourceNotFoundException("Saved meal not found.");
        }
    }

    // ── Copy ───────────────────────────────────────────────────────
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

    public boolean existsDailyMealItems(Integer userId, LocalDate mealDate, String mealName) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM meal_items mi
                JOIN meals m
                    ON m.id = mi.meal_id
                WHERE m.user_id = ?
                  AND m.meal_date = ?
                  AND m.name = ?
                  AND m.kind = 'DAILY'
            )
            """;

        Boolean exists = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                userId,
                mealDate,
                mealName
        );

        return Boolean.TRUE.equals(exists);
    }
}