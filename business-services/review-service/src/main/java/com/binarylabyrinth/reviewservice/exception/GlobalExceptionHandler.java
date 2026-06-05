package com.binarylabyrinth.reviewservice.exception;

import com.binarylabyrinth.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ReviewNotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(req, HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ReviewException.class)
    public ResponseEntity<ErrorResponse> handleReview(ReviewException ex, HttpServletRequest req) {
        // 409 because most ReviewExceptions are conflicts (duplicate review) or
        // ownership violations. The body's message is precise enough.
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(req, HttpStatus.CONFLICT, "CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(req, HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errs = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errs.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errs);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception on {} {}: ", req.getMethod(), req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error(req, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                        ex.getClass().getSimpleName() + ": " + ex.getMessage()));
    }

    private static ErrorResponse error(HttpServletRequest req, HttpStatus status, String code, String msg) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(code)
                .message(msg)
                .path(req.getRequestURI())
                .build();
    }
}
