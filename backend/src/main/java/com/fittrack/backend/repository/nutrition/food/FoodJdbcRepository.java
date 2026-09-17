package com.fittrack.backend.repository.nutrition.food;

import com.fittrack.backend.dto.nutrition.food.CreateFoodRequest;
import com.fittrack.backend.dto.nutrition.food.FoodResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FoodJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public FoodJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public FoodResponse createFood(Integer userId, CreateFoodRequest request) {
        String brand = request.brand();

        if (brand != null && brand.isBlank()) {
            brand = null;
        } else if (brand != null) {
            brand = brand.trim();
        }

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
                (resultSet, _) -> new FoodResponse(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("brand"),
                        resultSet.getDouble("serving_size_grams"),
                        resultSet.getDouble("calories_per_serving"),
                        resultSet.getDouble("protein_per_serving"),
                        resultSet.getDouble("carbs_per_serving"),
                        resultSet.getDouble("fat_per_serving"),
                        resultSet.getObject(
                                "created_by_user_id",
                                Integer.class
                        )
                ),
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
                        new IllegalArgumentException("User not found.")
                );
    }
}
