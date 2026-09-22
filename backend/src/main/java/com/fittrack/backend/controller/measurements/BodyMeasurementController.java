package com.fittrack.backend.controller.measurements;

import com.fittrack.backend.dto.measurements.body.BodyMeasurementRequest;
import com.fittrack.backend.dto.measurements.body.BodyMeasurementResponse;
import com.fittrack.backend.service.measurements.BodyMeasurementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/measurements/body")
@RequiredArgsConstructor
public class BodyMeasurementController {

    private final BodyMeasurementService bodyMeasurementService;

    @GetMapping("/user/{userId}")
    public List<BodyMeasurementResponse> getBodyMeasurementHistory(@PathVariable Integer userId) {
        return bodyMeasurementService.getBodyMeasurementHistory(userId);
    }

    @PostMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public BodyMeasurementResponse createBodyMeasurement(@PathVariable Integer userId, @Valid @RequestBody BodyMeasurementRequest request) {
        return bodyMeasurementService.createBodyMeasurement(userId, request);
    }

    @PutMapping("/user/{userId}/{bodyMeasurementId}")
    public BodyMeasurementResponse updateBodyMeasurement(@PathVariable Integer userId, @PathVariable Integer bodyMeasurementId, @Valid @RequestBody BodyMeasurementRequest request) {
        return bodyMeasurementService.updateBodyMeasurement(userId, bodyMeasurementId, request);
    }

    @DeleteMapping("/user/{userId}/{bodyMeasurementId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBodyMeasurement(@PathVariable Integer userId, @PathVariable Integer bodyMeasurementId) {
        bodyMeasurementService.deleteBodyMeasurement(userId, bodyMeasurementId);
    }
}