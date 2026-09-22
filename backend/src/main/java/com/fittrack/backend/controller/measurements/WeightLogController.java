package com.fittrack.backend.controller.measurements;

import com.fittrack.backend.dto.measurements.weight.WeightLogRequest;
import com.fittrack.backend.dto.measurements.weight.WeightLogResponse;
import com.fittrack.backend.service.measurements.WeightLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/measurements/weights")
@RequiredArgsConstructor
public class WeightLogController {

    private final WeightLogService weightLogService;

    @GetMapping("/user/{userId}")
    public List<WeightLogResponse> getWeightHistory(@PathVariable Integer userId) {
        return weightLogService.getWeightHistory(userId);
    }

    @PostMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public WeightLogResponse createWeightLog(@PathVariable Integer userId, @Valid @RequestBody WeightLogRequest request) {
        return weightLogService.createWeightLog(userId, request);
    }

    @PutMapping("/user/{userId}/{weightLogId}")
    public WeightLogResponse updateWeightLog(@PathVariable Integer userId, @PathVariable Integer weightLogId, @Valid @RequestBody WeightLogRequest request) {
        return weightLogService.updateWeightLog(userId, weightLogId, request);
    }

    @DeleteMapping("/user/{userId}/{weightLogId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWeightLog(@PathVariable Integer userId, @PathVariable Integer weightLogId) {
        weightLogService.deleteWeightLog(userId, weightLogId);
    }
}