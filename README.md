# Brokerage API

A Spring Boot application for managing stock orders in a brokerage firm. 
This API allows employees to create, list, and delete stock orders for customers, 
with comprehensive asset management and order matching capabilities with role privileges.

## Features

### Core Functionality
- **Order Management**: Create, list, and delete stock orders
- **Asset Management**: Track customer assets including TRY (Turkish Lira) and stocks
- **Order Status**: Support for PENDING, MATCHED, and CANCELED order statuses
- **Order Sides**: Support for BUY and SELL orders
- **Customer Authorization**: Role-based access control with admin and regular customer roles

### Bonus Features
- **Customer Authentication**: JWT-based authentication system
- **Customer Isolation**: Customers can only access their own data
- **Admin Order Matching**: Admin users can match pending orders
- **Asset Balance Updates**: Automatic asset balance management during order operations

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** with JWT authentication
- **Spring Data JPA** for data persistence
- **H2 Database** (in-memory for development)
- **Maven** for dependency management
- **Lombok** for reducing boilerplate code
- **JUnit 5** with Mockito for testing

## Project Structure

```
src/
├── main/
│   ├── java/com/brokerage/api/
│   │   ├── controller/         # REST API controllers
│   │   ├── service/            # Business logic services
│   │   ├── repository/         # Data access layer
│   │   ├── model/              # Entity classes
│   │   ├── dto/                # Data Transfer Objects
│   │   ├── config/             # Configuration classes
│   │   ├── security/           # Security configuration
│   │   └── exception/          # Custom exception classes
│   └── resources/
│       └── application.yml     # Application configuration
└── test/
    └── java/com/brokerage/api/
        ├── service/             # Service layer tests
        └── controller/          # Controller layer tests
```

## Database Schema

### Customer Table
- `id`: Primary key
- `username`: Unique username for login
- `password`: Encrypted password
- `fullName`: Customer's full name
- `email`: Customer's email address
- `isAdmin`: Boolean flag for admin privileges

### Asset Table
- `id`: Primary key
- `customerId`: Foreign key to Customer
- `assetName`: Name of the asset (e.g., "TRY", "AAPL", "GOOGL")
- `size`: Total asset balance
- `usableSize`: Available asset balance for trading

### Order Table
- `id`: Primary key
- `customerId`: Foreign key to Customer
- `assetName`: Name of the asset being traded
- `orderSide`: BUY or SELL
- `size`: Number of shares/amount
- `price`: Price per share/unit
- `status`: PENDING, MATCHED, or CANCELED
- `createDate`: Order creation timestamp

## API Endpoints

### Authentication
- `POST /api/v1/auth/login` - Customer login

### Orders
- `POST /api/v1/orders` - Create a new order
- `GET /api/v1/orders` - List orders for a customer with date range
- `DELETE /api/v1/orders/{orderId}` - Cancel a pending order
- `POST /api/v1/orders/{orderId}/match` - Match a pending order (Admin only)

### Assets
- `GET /api/v1/assets` - List assets for a customer

## API Documentation

### Create Order
```http
POST /api/v1/orders
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "customerId": 1,
  "assetName": "AAPL",
  "orderSide": "BUY",
  "size": 10,
  "price": 150.00
}
```

### List Orders
```http
GET /api/v1/orders?customerId=1&startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
Authorization: Bearer <jwt-token>
```

### Match Order (Admin Only)
```http
POST /api/v1/orders/1/match
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "orderId": 1
}
```

## Business Rules

1. **Order Creation**:
   - Orders are created with PENDING status
   - BUY orders require sufficient TRY balance
   - SELL orders require sufficient asset balance
   - TRY asset cannot be traded directly
   - Asset balances are reserved when orders are created

2. **Order Cancellation**:
   - Only PENDING orders can be canceled
   - Canceled orders restore reserved asset balances
   - Order status changes to "CANCELED"

3. **Order Matching**:
   - Only admin users can match orders
   - Matched orders update asset balances permanently
   - Order status changes to "MATCHED"

4. **Asset Management**:
   - TRY is the base currency for all transactions
   - Asset balances are automatically updated during order operations
   - Both total and usable balances are maintained

## Build and Run

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Build the Project
```bash
cd brokerage-api
mvn clean install
```

### Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Access H2 Console
- URL: `http://localhost:8080/api/v1/h2-console`
- JDBC URL: `jdbc:h2:mem:brokeragedb`
- Username: `sa`
- Password: `password`

## Sample Data

The application automatically creates sample data on startup:

### Admin User
- Username: `admin`
- Password: `admin123`
- Role: Administrator

### Sample Customers
- Username: `john.doe`, Password: `password123`
- Username: `jane.smith`, Password: `password123`
- Username: `bob.wilson`, Password: `password123`

### Initial Assets
Each customer starts with:
- 10,000 TRY in their account
- 100 AAPL shares
- 50 GOOGL shares

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run with Coverage
```bash
mvn jacoco:report
```

## Security

- **JWT Authentication**: Secure token-based authentication
- **Role-based Access Control**: Admin and customer roles
- **Customer Isolation**: Customers can only access their own data
- **Password Encryption**: BCrypt password hashing
- **CSRF Protection**: Enabled for state-changing operations

## Error Handling

The API includes a comprehensive exception handling system that provides meaningful error messages to the frontend:

- **Standardized Error Responses**: All errors follow a consistent format with error codes, messages, and timestamps
- **Meaningful Error Messages**: Clear, user-friendly error descriptions
- **Appropriate HTTP Status Codes**: Proper status codes for different types of errors
- **Error Tracking**: Unique error codes for debugging and support

For detailed information about error handling, see [EXCEPTION_HANDLING.md](EXCEPTION_HANDLING.md).

## Configuration

Key configuration options in `application.yml`:

```yaml
# JWT Configuration
jwt:
  secret: your-secret-key-here
  expiration: 86400000 # 24 hours

# Database Configuration
spring:
  datasource:
    url: jdbc:h2:mem:brokeragedb
    username: sa
    password: password

# Server Configuration
server:
  port: 8080
  servlet:
    context-path: /api/v1
```

## Production Considerations

1. **Database**: Replace H2 with a production database (PostgreSQL, MySQL, etc.)
2. **JWT Secret**: Use a strong, randomly generated secret key
3. **Logging**: Configure appropriate log levels for production
4. **Monitoring**: Add health checks and metrics
5. **Security**: Enable HTTPS and configure CORS appropriately
6. **Performance**: Add caching and connection pooling