package com.fittrack.backend.repository.nutrition.food;

import com.fittrack.backend.dto.nutrition.food.FoodRequest;
import com.fittrack.backend.dto.nutrition.food.FoodResponse;
import com.fittrack.backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FoodJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    // ── Read ───────────────────────────────────────────────────
    public List<FoodResponse> findTop20OrderByName() {
        String sql = """
            SELECT
                id,
                name,
                brand,
                serving_size_grams,
                calories_per_serving,
                protein_per_serving,
                carbs_per_serving,
                fat_per_serving,
                created_by_user_id
            FROM foods
            ORDER BY name
            LIMIT 20
            """;

        return jdbcTemplate.query(
                sql,
                this::mapFoodResponse
        );
    }

    public List<FoodResponse> findByUserIdOrderByName(Integer userId) {
        String sql = """
            SELECT
                id,
                name,
                brand,
                serving_size_grams,
                calories_per_serving,
                protein_per_serving,
                carbs_per_serving,
                fat_per_serving,
                created_by_user_id
            FROM foods
            WHERE created_by_user_id = ?
            ORDER BY name
            """;

        return jdbcTemplate.query(
                sql,
                this::mapFoodResponse,
                userId
        );
    }

    public List<FoodResponse> findTop20ByNameContaining(String name) {
        String sql = """
            SELECT
                id,
                name,
                brand,
                serving_size_grams,
                calories_per_serving,
                protein_per_serving,
                carbs_per_serving,
                fat_per_serving,
                created_by_user_id
            FROM foods
            WHERE LOWER(name) LIKE LOWER(CONCAT('%', ?, '%'))
            ORDER BY name
            LIMIT 20
            """;

        return jdbcTemplate.query(
                sql,
                this::mapFoodResponse,
                name
        );
    }

    public List<FoodResponse> findByUserIdAndNameContaining(Integer userId, String name) {
        String sql = """
            SELECT
                id,
                name,
                brand,
                serving_size_grams,
                calories_per_serving,
                protein_per_serving,
                carbs_per_serving,
                fat_per_serving,
                created_by_user_id
            FROM foods
            WHERE created_by_user_id = ?
              AND LOWER(name) LIKE LOWER(CONCAT('%', ?, '%'))
            ORDER BY name
            """;

        return jdbcTemplate.query(
                sql,
                this::mapFoodResponse,
                userId,
                name
        );
    }

    // ── Create ─────────────────────────────────────────────────
    public FoodResponse createFood(Integer userId, FoodRequest request) {
        String brand = normalizeBrand(request.brand());

        // Kreira food i vraca ga odmah, pa ne mora da se trazi sa findById
        String sql = """
                INSERT INTO foods (
                    name,
                    brand,
                    serving_size_grams,
                    calories_per_serving,
                    protein_per_serving,
                    carbs_per_serving,
                    fat_per_serving,
                    created_by_user_id,
                    created_at
                )
                SELECT
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    u.id,
                    CURRENT_TIMESTAMP
                FROM users u
                WHERE u.id = ?
                RETURNING
                    id,
                    name,
                    brand,
                    serving_size_grams,
                    calories_per_serving,
                    protein_per_serving,
                    carbs_per_serving,
                    fat_per_serving,
                    created_by_user_id
                """;

        List<FoodResponse> results = jdbcTemplate.query(
                sql,
                this::mapFoodResponse,
                request.name().trim(),
                brand,
                request.servingSizeGrams(),
                request.caloriesPerServing(),
                request.proteinPerServing(),
                request.carbsPerServing(),
                request.fatPerServing(),
                userId
        );

        return results.stream()
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found.")
                );
    }

    // ── Update ─────────────────────────────────────────────────
    public FoodResponse updateFood(Integer userId, Integer foodId, FoodRequest request) {
        String brand = normalizeBrand(request.brand());

        // Azurira food i vraca ga odmah, pa ne mora da se trazi sa findById
        String sql = """
                UPDATE foods
                SET
                    name = ?,
                    brand = ?,
                    serving_size_grams = ?,
                    calories_per_serving = ?,
                    protein_per_serving = ?,
                    carbs_per_serving = ?,
                    fat_per_serving = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                  AND created_by_user_id = ?
                RETURNING
                    id,
                    name,
                    brand,
                    serving_size_grams,
                    calories_per_serving,
                    protein_per_serving,
                    carbs_per_serving,
                    fat_per_serving,
                    created_by_user_id
                """;

        List<FoodResponse> results = jdbcTemplate.query(
                sql,
                this::mapFoodResponse,
                request.name().trim(),
                brand,
                request.servingSizeGrams(),
                request.caloriesPerServing(),
                request.proteinPerServing(),
                request.carbsPerServing(),
                request.fatPerServing(),
                foodId,
                userId
        );

        return results.stream()
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Food not found.")
                );
    }

    // ── Delete ─────────────────────────────────────────────────
    public void deleteFood(Integer userId, Integer foodId) {
        String sql = """
                DELETE FROM foods
                WHERE id = ?
                  AND created_by_user_id = ?
                RETURNING id
                """;

        List<Integer> deletedIds = jdbcTemplate.query(
                sql,
                (resultSet, _) ->
                        resultSet.getInt("id"),
                foodId,
                userId
        );

        if (deletedIds.isEmpty()) {
            throw new ResourceNotFoundException("Food not found.");
        }
    }

    // ── Helpers ─────────────────────────────────────────────────
    private String normalizeBrand(String brand) {
        if (brand == null || brand.isBlank()) {
            return null;
        }

        return brand.trim();
    }

    private FoodResponse mapFoodResponse(ResultSet resultSet, int rowNum) throws SQLException {
        return new FoodResponse(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("brand"),
                resultSet.getDouble("serving_size_grams"),
                resultSet.getDouble("calories_per_serving"),
                resultSet.getDouble("protein_per_serving"),
                resultSet.getDouble("carbs_per_serving"),
                resultSet.getDouble("fat_per_serving"),
                resultSet.getObject("created_by_user_id", Integer.class)
        );
    }
}