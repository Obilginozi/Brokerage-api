# Exception Handling in Brokerage API

This document explains how exceptions are handled in the Brokerage API to provide meaningful error messages to the frontend.

## Overview

The API now includes a comprehensive exception handling system that catches all exceptions thrown by services and controllers, and returns standardized error responses with meaningful messages, error codes, and appropriate HTTP status codes.

## Components

### 1. ErrorResponse DTO

The `ErrorResponse` class provides a standardized structure for all error responses:

```java
{
  "errorCode": "ORDER_001",
  "message": "Insufficient TRY balance. Required: 1500.00, Available: 1000.00",
  "details": null,
  "timestamp": "2025-08-25 00:21:43",
  "path": "uri=/orders"
}
```

**Fields:**
- `errorCode`: A unique identifier for the type of error
- `message`: Human-readable error message
- `details`: Additional error details (if available)
- `timestamp`: When the error occurred
- `path`: The endpoint that caused the error

### 2. Global Exception Handler

The `GlobalExceptionHandler` class uses Spring's `@RestControllerAdvice` to catch all exceptions across the application and convert them to appropriate HTTP responses.

## Exception Types and Error Codes

### Authentication Errors (AUTH_*)
- **AUTH_001**: Authentication failed (Invalid username/password)
- **AUTH_002**: Access denied (Insufficient permissions)
- **AUTH_003**: JWT authentication failed

### Order Errors (ORDER_*)
- **ORDER_001**: Insufficient funds
- **ORDER_002**: Invalid order (validation errors)
- **ORDER_003**: Order not found

### Customer Errors (CUSTOMER_*)
- **CUSTOMER_001**: Customer not found

### Validation Errors (VALIDATION_*)
- **VALIDATION_001**: Request validation failed
- **VALIDATION_002**: Illegal argument

### Internal Errors (INTERNAL_*)
- **INTERNAL_001**: Unexpected server error

## HTTP Status Codes

The exception handler maps different exception types to appropriate HTTP status codes:

- **400 Bad Request**: Validation errors, insufficient funds, invalid orders
- **401 Unauthorized**: Authentication failures, JWT errors
- **403 Forbidden**: Access denied
- **404 Not Found**: Resources not found (orders, customers)
- **500 Internal Server Error**: Unexpected errors

## Example Error Responses

### 1. Insufficient Funds
```json
{
  "errorCode": "ORDER_001",
  "message": "Insufficient TRY balance. Required: 1500.00, Available: 1000.00",
  "details": null,
  "timestamp": "2025-08-25 00:21:43",
  "path": "uri=/orders"
}
```

### 2. Validation Error
```json
{
  "errorCode": "VALIDATION_001",
  "message": "Validation failed",
  "details": "Size must be greater than 0, Price must be greater than 0",
  "timestamp": "2025-08-25 00:21:43",
  "path": "uri=/orders"
}
```

### 3. Authentication Error
```json
{
  "errorCode": "AUTH_001",
  "message": "Invalid username or password",
  "details": null,
  "timestamp": "2025-08-25 00:21:43",
  "path": "uri=/auth/login"
}
```

### 4. Access Denied
```json
{
  "errorCode": "AUTH_002",
  "message": "Access denied. You don't have permission to perform this action.",
  "details": null,
  "timestamp": "2025-08-25 00:21:43",
  "path": "uri=/orders/1/match"
}
```

## Custom Exceptions

The API includes several custom exceptions that provide meaningful error messages:

### AuthenticationException
Thrown when login credentials are invalid.

### InsufficientFundsException
Thrown when a customer doesn't have enough funds to place an order.

### InvalidOrderException
Thrown when order validation fails (e.g., invalid size, price, or asset).

### OrderNotFoundException
Thrown when trying to access an order that doesn't exist.

### CustomerNotFoundException
Thrown when trying to access a customer that doesn't exist.

### JwtAuthenticationException
Thrown when JWT token validation fails.

## Frontend Integration

### Error Handling
Frontend applications should check the HTTP status code and parse the error response body to display meaningful error messages to users.

### Example Frontend Code
```javascript
try {
  const response = await fetch('/api/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(orderData)
  });
  
  if (!response.ok) {
    const errorData = await response.json();
    // Display error message to user
    showError(`${errorData.errorCode}: ${errorData.message}`);
  } else {
    const order = await response.json();
    // Handle success
  }
} catch (error) {
  // Handle network errors
  showError('Network error occurred');
}
```

### Error Display
- Use the `errorCode` for logging and debugging
- Display the `message` to the user
- Show `details` if available for additional context
- Use the `timestamp` for audit trails

## Testing

The exception handling system includes comprehensive tests in `GlobalExceptionHandlerTest` that verify:
- Correct HTTP status codes are returned
- Error codes are properly set
- Error messages are preserved
- Path information is included

## Benefits

1. **Consistent Error Format**: All errors follow the same structure
2. **Meaningful Messages**: Users get clear information about what went wrong
3. **Error Tracking**: Error codes help with debugging and support
4. **Security**: Sensitive information is not exposed in error messages
5. **User Experience**: Frontend can provide better error handling and user feedback

## Future Enhancements

Potential improvements to consider:
- Internationalization (i18n) support for error messages
- Error severity levels
- Error categorization for analytics
- Rate limiting error responses
- Custom error pages for web clients
