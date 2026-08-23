package com.fittrack.backend.repository.nutrition;

import com.fittrack.backend.entity.nutrition.Food;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FoodRepository extends JpaRepository<@NonNull Food, @NonNull Integer> {

    List<Food> findTop20ByOrderByNameAsc();

    @Query(
            value = """
                    SELECT *
                    FROM foods
                    WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))
                    ORDER BY name
                    LIMIT 20
                    """,
            nativeQuery = true
    )
    List<Food> findTop20ByNameContainingIgnoreCaseOrderByNameAsc(
            @Param("name") String name
    );

    List<Food> findByCreatedByUserIdOrderByNameAsc(Integer userId);

    @Query(
            value = """
                    SELECT *
                    FROM foods
                    WHERE created_by_user_id = :userId
                      AND LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))
                    ORDER BY name
                    """,
            nativeQuery = true
    )
    List<Food> findByCreatedByUserIdAndNameContainingIgnoreCaseOrderByNameAsc(
            @Param("userId") Integer userId,
            @Param("name") String name
    );
}
