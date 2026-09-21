package com.fittrack.backend.service.profile;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class ProfileValidationService {

    private static final int MIN_AGE = 13;
    private static final int MAX_AGE = 120;

    public int validateAge(LocalDate dateOfBirth, LocalDate today) {
        int age = Period.between(
                dateOfBirth,
                today
        ).getYears();

        if (age < MIN_AGE || age > MAX_AGE) {
            throw new IllegalArgumentException("Age must be between 13 and 120.");
        }

        return age;
    }
}
