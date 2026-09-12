package com.fittrack.backend.dto.nutrition.meal;

import com.fittrack.backend.dto.nutrition.meal.item.UpdateSavedMealItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateSavedMealRequest(
        @NotBlank @Size(max = 255) String name,
        @NotEmpty List<@Valid UpdateSavedMealItemRequest> items
) {}
