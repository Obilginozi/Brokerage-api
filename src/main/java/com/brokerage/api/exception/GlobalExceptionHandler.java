package com.brokerage.api.exception;

import com.brokerage.api.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.error("Authentication error: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of("AUTH_001", ex.getMessage());
        error.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleJwtAuthenticationException(JwtAuthenticationException ex, WebRequest request) {
        log.error("JWT authentication error: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of("AUTH_003", ex.getMessage());
        error.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFundsException(InsufficientFundsException ex, WebRequest request) {
        log.error("Insufficient funds error: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of("ORDER_001", ex.getMessage());
        error.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderException(InvalidOrderException ex, WebRequest request) {
        log.error("Invalid order error: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of("ORDER_002", ex.getMessage());
        error.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException ex, WebRequest request) {
        log.error("Order not found error: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of("ORDER_003", ex.getMessage());
        error.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(CustomerNotFoundException ex, WebRequest request) {
        log.error("Customer not found error: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of("CUSTOMER_001", ex.getMessage());
        error.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.error("Access denied error: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of("AUTH_002", "Access denied. You don't have permission to perform this action.");
        error.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation error: {}", ex.getMessage());
        
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        ErrorResponse error = ErrorResponse.of("VALIDATION_001", "Validation failed", details);
        error.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        ErrorResponse error = ErrorResponse.of("INTERNAL_001", "An unexpected error occurred. Please try again later.");
        error.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.error("Illegal argument error: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of("VALIDATION_002", ex.getMessage());
        error.setPath(request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
