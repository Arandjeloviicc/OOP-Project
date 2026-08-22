package com.fittrack.backend.dto.nutrition.meal;

import com.fittrack.backend.dto.nutrition.meal.item.CreateMealItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateMealRequest(
        @NotBlank String name,
        @NotEmpty List<@Valid CreateMealItemRequest> items
) {}
