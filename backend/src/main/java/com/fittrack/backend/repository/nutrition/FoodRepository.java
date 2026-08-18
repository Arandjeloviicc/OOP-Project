package com.fittrack.backend.repository.nutrition;

import com.fittrack.backend.entity.nutrition.Food;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodRepository extends JpaRepository<@NonNull Food, @NonNull Integer> {

    List<Food> findTop20ByOrderByNameAsc();

    List<Food> findTop20ByNameContainingIgnoreCaseOrderByNameAsc(String name);

    List<Food> findByCreatedByUser_IdOrderByNameAsc(Integer userId);
}
