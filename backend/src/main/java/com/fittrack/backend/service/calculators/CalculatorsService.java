package com.fittrack.backend.service.calculators;

import com.fittrack.backend.dto.calculators.CalculatorsResponse;
import com.fittrack.backend.repository.calculators.CalculatorsJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalculatorsService {

    private final CalculatorsJdbcRepository calculatorJdbcRepository;

    public CalculatorsResponse getCalculatorsData(Integer userId) {
        return calculatorJdbcRepository.findDataByUserId(userId);
    }
}