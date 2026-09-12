package com.fittrack.backend.dto.nutrition.meal;

import com.fittrack.backend.dto.nutrition.meal.item.CreateMealItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateMealRequest(
        @NotBlank @Size(max = 255) String name,
        @NotEmpty List<@Valid CreateMealItemRequest> items
) {}
