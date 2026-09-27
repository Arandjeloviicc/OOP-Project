package com.fittrack.backend.controller.calculators;

import com.fittrack.backend.dto.calculators.CalculatorsResponse;
import com.fittrack.backend.service.calculators.CalculatorsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calculators")
@RequiredArgsConstructor
public class CalculatorsController {

    private final CalculatorsService calculatorService;

    @GetMapping("/user/{userId}")
    public CalculatorsResponse getCalculatorsData(@PathVariable Integer userId) {
        return calculatorService.getCalculatorsData(userId);
    }
}
