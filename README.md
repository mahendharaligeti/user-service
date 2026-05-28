# User Service - Hospital Management System

A production-ready Spring Boot 3.x microservice for hospital user management with JWT authentication.

## Features

- **Spring Boot 3.2.0** with Java 17
- **JWT Authentication** with role-based access control
- **Spring Security** with BCrypt password encoding
- **H2 Database** for testing, MySQL for development/production
- **Structured JSON Logging** with request/correlation IDs
- **OpenTelemetry OTLP Tracing** for APIs, repository/database calls, and outbound HTTP clients
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
# Create local dev database first
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS UserService;"

# Development
export DB_USERNAME="root"
export DB_PASSWORD="your-local-password"
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Production
export JWT_SECRET="your-256-bit-secret"
export DB_URL="jdbc:mysql://your-host:3306/user_service_db"
export DB_USERNAME="root"
export DB_PASSWORD="password"
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

### Observability
Logs are emitted as JSON and include `requestId`/`correlationId` when requests pass through the API. Send either `X-Correlation-Id` or `X-Request-Id` from clients to reuse an existing request ID; otherwise the service creates one and returns it in both response headers.

Traces are exported over OTLP HTTP. By default the service sends traces to `http://localhost:4318/v1/traces`.

```bash
export OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4318/v1/traces"
export TRACING_SAMPLING_PROBABILITY="1.0"
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

The service traces inbound API calls automatically through Spring Boot Actuator/Micrometer, wraps repository/database calls with named observations such as `db.user.find-by-id`, and provides an observed `RestTemplate` bean for future service-to-service HTTP calls.

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
