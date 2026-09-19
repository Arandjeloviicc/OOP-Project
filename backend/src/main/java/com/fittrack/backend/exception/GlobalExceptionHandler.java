package com.fittrack.backend.exception;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<@NonNull ApiError> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(
                new ApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        exception.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull ApiError> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed.");

        return ResponseEntity.badRequest().body(
                new ApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        message,
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<@NonNull ApiError> handleDataIntegrity() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ApiError(
                        HttpStatus.CONFLICT.value(),
                        "Database constraint violation.",
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<@NonNull ApiError> handleIllegalState(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ApiError(
                        HttpStatus.CONFLICT.value(),
                        exception.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<@NonNull ApiError> handleNotFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        exception.getMessage(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NonNull ApiError> handleUnexpected(Exception exception) {
        log.error("Unhandled exception", exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiError(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "An unexpected error occurred.",
                        LocalDateTime.now()
                )
        );
    }
}
