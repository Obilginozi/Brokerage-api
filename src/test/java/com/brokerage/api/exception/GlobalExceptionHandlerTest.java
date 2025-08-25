package com.brokerage.api.exception;

import com.brokerage.api.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private ServletWebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/test");
        webRequest = new ServletWebRequest(request);
    }

    @Test
    void handleAuthenticationException_ShouldReturnUnauthorizedStatus() {
        // Given
        AuthenticationException ex = new AuthenticationException("Invalid credentials");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("AUTH_001", response.getBody().getErrorCode());
        assertEquals("Invalid credentials", response.getBody().getMessage());
        assertEquals("uri=/test", response.getBody().getPath());
    }

    @Test
    void handleInsufficientFundsException_ShouldReturnBadRequestStatus() {
        // Given
        InsufficientFundsException ex = new InsufficientFundsException("Insufficient funds");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInsufficientFundsException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ORDER_001", response.getBody().getErrorCode());
        assertEquals("Insufficient funds", response.getBody().getMessage());
    }

    @Test
    void handleInvalidOrderException_ShouldReturnBadRequestStatus() {
        // Given
        InvalidOrderException ex = new InvalidOrderException("Invalid order");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidOrderException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ORDER_002", response.getBody().getErrorCode());
        assertEquals("Invalid order", response.getBody().getMessage());
    }

    @Test
    void handleOrderNotFoundException_ShouldReturnNotFoundStatus() {
        // Given
        OrderNotFoundException ex = new OrderNotFoundException("Order not found");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleOrderNotFoundException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ORDER_003", response.getBody().getErrorCode());
        assertEquals("Order not found", response.getBody().getMessage());
    }

    @Test
    void handleCustomerNotFoundException_ShouldReturnNotFoundStatus() {
        // Given
        CustomerNotFoundException ex = new CustomerNotFoundException("Customer not found");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleCustomerNotFoundException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CUSTOMER_001", response.getBody().getErrorCode());
        assertEquals("Customer not found", response.getBody().getMessage());
    }

    @Test
    void handleAccessDeniedException_ShouldReturnForbiddenStatus() {
        // Given
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("AUTH_002", response.getBody().getErrorCode());
        assertEquals("Access denied. You don't have permission to perform this action.", response.getBody().getMessage());
    }

    @Test
    void handleValidationException_ShouldReturnBadRequestStatus() throws NoSuchMethodException {
        // Given
        FieldError fieldError = new FieldError("object", "field", "Field is required");
        org.springframework.validation.BeanPropertyBindingResult bindingResult =
            new org.springframework.validation.BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(fieldError);

        // Create a mock MethodParameter
        org.springframework.core.MethodParameter methodParameter =
            new org.springframework.core.MethodParameter(
                this.getClass().getDeclaredMethod("setUp"), -1
            );

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_001", response.getBody().getErrorCode());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals("Field is required", response.getBody().getDetails());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerErrorStatus() {
        // Given
        Exception ex = new Exception("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_001", response.getBody().getErrorCode());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequestStatus() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_002", response.getBody().getErrorCode());
        assertEquals("Invalid argument", response.getBody().getMessage());
    }

    @Test
    void handleJwtAuthenticationException_ShouldReturnUnauthorizedStatus() {
        // Given
        JwtAuthenticationException ex = new JwtAuthenticationException("Invalid JWT token");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleJwtAuthenticationException(ex, webRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("AUTH_003", response.getBody().getErrorCode());
        assertEquals("Invalid JWT token", response.getBody().getMessage());
    }
}
