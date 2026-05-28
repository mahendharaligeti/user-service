# User Service - Hospital Management System

A production-ready Spring Boot 3.x microservice for hospital user management with JWT authentication.

## Features

- **Spring Boot 3.2.0** with Java 17
- **JWT Authentication** with role-based access control
- **Spring Security** with BCrypt password encoding
- **H2 Database** for testing, MySQL for development/production
- **Comprehensive Test Coverage** (Unit & Integration tests with JUnit 5)
- **Environment-based Configuration** (dev, test, prod profiles)
- **RESTful API** with proper exception handling

## Project Structure

```
src/main/java/com/hospital/userservice/
├── config/              # Security, JWT, UserDetails
├── controller/          # REST endpoints
├── dto/                 # Request/Response DTOs
├── entity/              # JPA entities
├── exception/           # Custom exceptions
├── repository/          # Data access layer
├── service/             # Business logic
└── util/                # JWT utilities
```

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+ (for dev/prod)

### Build
```bash
mvn clean install
```

### Run
```bash
# Development
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Production
export JWT_SECRET="your-256-bit-secret"
export DB_URL="jdbc:mysql://your-host:3306/user_service_db"
export DB_USERNAME="root"
export DB_PASSWORD="password"
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

### Test
```bash
mvn test
```

## API Endpoints

### Authentication (Public)
- `POST /api/auth/register` - Register new user (HTTP 201)
- `POST /api/auth/login` - Login user (HTTP 200)

### Users (Authenticated)
- `GET /api/users` - Get all users (ADMIN only)
- `GET /api/users/{id}` - Get user by ID (ADMIN only)
- `GET /api/users/role/{role}` - Get users by role (ADMIN/DOCTOR)

### Health
- `GET /actuator/health` - Health check (public)

## Configuration Files

- `application.yml` - Base configuration
- `application-dev.yml` - Development (local MySQL)
- `application-prod.yml` - Production (RDS with env vars)
- `application-test.yml` - Test (H2 in-memory)

## Testing

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=UserServiceImplTest

# Integration tests only
mvn test -Dtest=*IntegrationTest
```

## Security

- JWT tokens expire after 24 hours
- Passwords are BCrypt-hashed (strength 10)
- CSRF disabled (stateless microservice)
- Session management is stateless
- Role-based access control with @PreAuthorize annotations

## License

Proprietary - Hospital Management System
