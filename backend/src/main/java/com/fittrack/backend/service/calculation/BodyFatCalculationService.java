package com.fittrack.backend.service.calculation;

import com.fittrack.backend.entity.profile.Gender;
import org.springframework.stereotype.Service;

@Service
public class BodyFatCalculationService {

    // ── Body Fat (US Navy Method) ─────────────────────────────────────────
    public double calculate(Gender gender, double heightCm, double neckCm, double waistCm, Double hipCm) {
        validate(gender, heightCm, neckCm, waistCm, hipCm);

        if (gender == Gender.FEMALE) {
            return Math.max(0, calculateBodyFatFemale(heightCm, neckCm, waistCm, hipCm));
        }

        return Math.max(0, calculateBodyFatMale(heightCm, neckCm, waistCm));
    }

    // ── Male ─────────────────────────────────────────────────
    private static double calculateBodyFatMale(double heightCm, double neckCm, double waistCm) {
        return (495 / (1.0324 - 0.19077 * Math.log10(waistCm - neckCm) + 0.15456 * Math.log10(heightCm))) - 450;
    }

    // ── Female ─────────────────────────────────────────────────
    private static double calculateBodyFatFemale(double heightCm, double neckCm, double waistCm, double hipCm) {
        return (495 / (1.29579 - 0.35004 * Math.log10(waistCm + hipCm - neckCm) + 0.22100 * Math.log10(heightCm))) - 450;
    }

    // ── Validate ─────────────────────────────────────────────────
    public void validate(Gender gender, double heightCm, double neckCm, double waistCm, Double hipCm) {
        if (heightCm <= 0 || neckCm <= 0 || waistCm <= 0) {
            throw new IllegalArgumentException("Body measurements must be greater than zero.");
        }

        if (gender == Gender.FEMALE) {
            if (hipCm == null || hipCm <= 0) {
                throw new IllegalArgumentException("Hip measurement is required for female body fat calculation.");
            }

            if (waistCm + hipCm <= neckCm) {
                throw new IllegalArgumentException("Invalid measurements for body fat calculation.");
            }

            return;
        }

        if (waistCm <= neckCm) {
            throw new IllegalArgumentException("Waist measurement must be greater than neck measurement.");
        }
    }
}