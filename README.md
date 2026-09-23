# Microservices Architecture Project

A comprehensive Spring Boot microservices implementation demonstrating modern architectural patterns including API Gateway, JWT authentication, service-to-service communication (gRPC), event-driven architecture (Kafka), and REST APIs.

## Architecture Overview

This project consists of five microservices that communicate using REST, gRPC, and Kafka through an API Gateway:

- **API Gateway** - Entry point routing requests to backend services with JWT validation
- **Auth Service** - Authentication service providing JWT token generation and validation
- **Patient Service** - Primary service exposing REST API endpoints, gRPC client, and Kafka producer
- **Billing Service** - Secondary service exposing gRPC endpoints for billing account creation
- **Analytics Service** - Event-driven service consuming patient events via Kafka

```
                              ┌─────────────────────┐
                              │    API Gateway      │
                              │    Port 4004        │
                              └──────────┬──────────┘
                                         │
                    ┌────────────────────┼────────────────────┐
                    │                    │                    │
                    ▼                    ▼                    │
        ┌─────────────────────┐  ┌─────────────────────┐      │
        │   Auth Service      │  │  Patient Service    │      │
        │   Port 4005         │  │  Port 4000          │      │
        │  (JWT Auth)         │  │  (REST API)         │      │
        └─────────────────────┘  └──────────┬──────────┘      │
                                            │                 │
                                   gRPC     │  Kafka          │
                                            │  Events         │
                    ┌───────────────────────┼────────┐        │
                    ▼                       ▼        ▼        │
        ┌─────────────────────┐    ┌──────────────────────┐   │
        │  Billing Service    │    │ Analytics Service    │   │
        │  (gRPC Server)      │    │ (Kafka Consumer)     │   │
        │  Port 4001          │    │ Port 4002            │   │
        │  gRPC Port 9001     │    └──────────────────────┘   │
        └─────────────────────┘             ▲                 │
                                            │                 │
                                       ┌────────┐             │
                                       │ Kafka  │  ◄──────────┘
                                       │ Broker │
                                       └────────┘
```

## Technology Stack

### Backend Framework
- **Spring Boot 3.4.x** - Main application framework
- **Spring Cloud Gateway** - API Gateway implementation
- **Spring Security** - Authentication and authorization
- **Java 21** - Programming language
- **Maven** - Dependency management and build tool

### Communication Protocols
- **REST API** - HTTP-based API for client communication
- **gRPC** - High-performance RPC framework for inter-service communication
- **Apache Kafka** - Distributed event streaming platform for asynchronous messaging
- **Protocol Buffers (Proto3)** - Interface definition and serialization

### Security
- **JWT (JSON Web Tokens)** - Stateless authentication mechanism
- **BCrypt** - Password hashing
- **Spring Security** - Security framework

### Data Persistence
- **Spring Data JPA** - Data access abstraction
- **PostgreSQL** - Production database (configurable)
- **H2 Database** - In-memory database for testing (optional)

### Validation & Documentation
- **Jakarta Validation** - Request validation with custom validation groups
- **SpringDoc OpenAPI 3** (Swagger) - API documentation and testing UI

### Infrastructure
- **Docker** - Containerization with multi-stage builds
- **Docker Compose** - Multi-container orchestration (optional)
- **Maven Wrapper** - Ensures consistent Maven version across environments

## Project Structure

```
.
├── api-gateway/              # API Gateway service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/pm/apigateway/
│   │   │   │   ├── filter/          # JWT validation filter
│   │   │   │   └── exception/       # Exception handlers
│   │   │   └── resources/
│   │   │       └── application.yml  # Gateway routing config
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── auth-service/             # Authentication service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/pm/authservice/
│   │   │   │   ├── controller/      # Auth endpoints
│   │   │   │   ├── service/         # Auth logic
│   │   │   │   ├── repository/      # User data access
│   │   │   │   ├── model/           # User entity
│   │   │   │   ├── dto/             # Login request/response
│   │   │   │   ├── config/          # Security configuration
│   │   │   │   └── util/            # JWT utilities
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── data.sql         # Sample users
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── patient-service/          # Primary REST API service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/pm/patientservice/
│   │   │   │   ├── controller/      # REST controllers
│   │   │   │   ├── service/         # Business logic
│   │   │   │   ├── repository/      # Data access layer
│   │   │   │   ├── model/           # JPA entities
│   │   │   │   ├── dto/             # Data transfer objects
│   │   │   │   ├── mapper/          # DTO-Entity converters
│   │   │   │   ├── grpc/            # gRPC client
│   │   │   │   ├── kafka/           # Kafka producer
│   │   │   │   └── exception/       # Custom exceptions & handlers
│   │   │   ├── proto/               # Protocol Buffer definitions
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── data.sql         # Sample data
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── billing-service/          # gRPC service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/pm/billingservice/
│   │   │   │   └── grpc/            # gRPC service implementation
│   │   │   ├── proto/               # Protocol Buffer definitions
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── analytics-service/        # Kafka consumer service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/pm/analyticsservice/
│   │   │   │   └── kafka/           # Kafka consumer
│   │   │   ├── proto/               # Protocol Buffer definitions
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── api-requests/             # HTTP request examples
│   ├── auth-service/
│   │   ├── login.http
│   │   └── validate.http
│   └── patient-service/
│       ├── create-patient.http
│       ├── get-patients.http
│       ├── update-patient.http
│       └── delete-patient.http
│
├── grpc-requests/            # gRPC request examples
│   └── billing-service/
│       └── create-billing-account.http
│
└── dockerFileConfig/         # IntelliJ Docker run configurations
    ├── api-gateway.run.xml
    ├── auth-service.run.xml
    ├── patient-service.run.xml
    ├── billing-service.run.xml
    ├── analytics-service.run.xml
    ├── kafka.run.xml
    └── patient-service-db.run.xml
```

## Key Features

### API Gateway (Port 4004)

**Routing Configuration:**
- `/auth/**` → Auth Service (Port 4005)
- `/api/patients/**` → Patient Service (Port 4000) with JWT validation
- `/api-docs/patients` → Patient Service OpenAPI docs
- `/api-docs/auth` → Auth Service OpenAPI docs

**Technical Highlights:**
- Spring Cloud Gateway for reactive routing
- Custom JWT validation filter using WebClient
- Global exception handling for unauthorized requests
- Path stripping and rewriting for clean API routes
- Service discovery via environment variables

### Auth Service (Port 4005)

**REST API Endpoints:**
- `POST /login` - Generate JWT token with user credentials
- `GET /validate` - Validate JWT token

**Technical Highlights:**
- JWT token generation and validation using JJWT library
- BCrypt password hashing for secure storage
- PostgreSQL integration for user management
- 10-hour token expiration
- Base64-encoded secret key configuration
- Role-based claims in JWT payload
- Sample user pre-loaded: `testuser@test.com` / `password123`

### Patient Service (Port 4000)

**REST API Endpoints:**
- `GET /patients` - Retrieve all patients
- `POST /patients` - Create new patient
- `PUT /patients/{id}` - Update existing patient
- `DELETE /patients/{id}` - Delete patient

**Technical Highlights:**
- UUID-based primary keys
- Email uniqueness validation
- Custom validation groups for create vs update operations
- Global exception handling with custom exceptions
- Automatic OpenAPI/Swagger documentation at `/swagger-ui.html`
- gRPC client for inter-service communication with Billing Service
- Kafka producer for publishing patient events
- PostgreSQL integration with sample data initialization
- 15 pre-loaded sample patients for testing

### Billing Service (Ports 4001 & 9001)

**gRPC Service:**
- `CreateBillingAccount` - RPC method for account creation

**Technical Highlights:**
- gRPC server implementation using `grpc-spring-boot-starter`
- Protocol Buffer message definitions
- Blocking stub for synchronous communication
- Dual port configuration (HTTP: 4001, gRPC: 9001)
- Lightweight service focused on billing account creation

### Analytics Service (Port 4002)

**Kafka Consumer:**
- Consumes patient events from the `patient` topic
- Processes events for analytics and reporting purposes

**Technical Highlights:**
- Event-driven architecture using Apache Kafka
- Protocol Buffer deserialization for event processing
- Consumer group: `analytics-service`
- Asynchronous message processing
- Logs received patient events for monitoring

## Communication Patterns

### 1. API Gateway Routing (Client → Gateway → Services)
All client requests flow through the API Gateway, which routes to appropriate services and validates JWT tokens for protected endpoints.

### 2. JWT Authentication (Client → Auth Service)
Clients authenticate with credentials to receive JWT tokens, which are validated by the gateway for subsequent requests.

### 3. REST API (Gateway → Patient Service)
Standard HTTP REST endpoints for CRUD operations on patient data, protected by JWT validation.

### 4. gRPC (Patient Service → Billing Service)
Synchronous RPC calls for creating billing accounts when new patients are registered.

**Protocol Buffer Definition:**
```protobuf
syntax = "proto3";

option java_multiple_files = true;
option java_package = "billing";

service BillingService {
  rpc CreateBillingAccount (BillingRequest) returns (BillingResponse);
}

message BillingRequest {
  string patientId = 1;
  string name = 2;
  string email = 3;
}

message BillingResponse {
  string accountId = 1;
  string status = 2;
}
```

### 5. Kafka Events (Patient Service → Analytics Service)
Asynchronous event publishing for patient creation events.

**Event Schema:**
```protobuf
syntax = "proto3";

package patient.events;

message PatientEvent {
  string patientId = 1;
  string name = 2;
  string email = 3;
  string event_type = 4;
}
```

## Configuration

### API Gateway (Port 4004)

```yaml
server:
  port: 4004

spring:
  cloud:
    gateway:
      routes:
        - id: auth-service-route
          uri: http://auth-service:4005
          predicates:
            - Path=/auth/**
          filters:
            - StripPrefix=1
            
        - id: patient-service-route
          uri: http://patient-service:4000
          predicates:
            - Path=/api/patients/**
          filters:
            - StripPrefix=1
            - JwtValidation
```

### Auth Service (Port 4005)

```properties
spring.application.name=auth-service
server.port=4005

# JWT Configuration (via environment variable)
jwt.secret=<base64-encoded-secret>

# Database configuration
spring.datasource.url=jdbc:postgresql://auth-service-db:5432/db
spring.datasource.username=admin_user
spring.datasource.password=password
```

### Patient Service (Port 4000)

```properties
spring.application.name=patient-service
server.port=4000
logging.level.root=info

# gRPC client configuration
billing.service.address=localhost
billing.service.grcp.port=9001

# Kafka producer configuration
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.ByteArraySerializer

# Database configuration (PostgreSQL)
spring.datasource.url=jdbc:postgresql://patient-service-db:5432/db
```

### Billing Service (Ports 4001 & 9001)

```properties
spring.application.name=billing-service
server.port=4001
grpc.server.port=9001
```

### Analytics Service (Port 4002)

```properties
spring.application.name=analytics-service

# Kafka consumer configuration
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer
```

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.9+** (or use included Maven wrapper)
- **Docker** (optional, for containerized deployment)
- **PostgreSQL** (for patient-service and auth-service databases)
- **Apache Kafka** (for event streaming)

### Running Locally

#### 1. Start Infrastructure Services

**PostgreSQL for Patient Service:**
```bash
docker run -d \
  --name patient-service-db \
  -e POSTGRES_USER=admin_user \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=db \
  -p 5000:5432 \
  postgres:latest
```

**PostgreSQL for Auth Service:**
```bash
docker run -d \
  --name auth-service-db \
  -e POSTGRES_USER=admin_user \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=db \
  -p 5001:5432 \
  postgres:latest
```

**Kafka (KRaft mode):**
```bash
docker run -d \
  --name kafka \
  -p 9092:9092 \
  -p 9094:9094 \
  -e KAFKA_CFG_NODE_ID=0 \
  -e KAFKA_CFG_PROCESS_ROLES=controller,broker \
  -e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@kafka:9093 \
  -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093,EXTERNAL://:9094 \
  -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,EXTERNAL://localhost:9094 \
  -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,EXTERNAL:PLAINTEXT,PLAINTEXT:PLAINTEXT \
  -e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  bitnamilegacy/kafka:latest
```

#### 2. Build Services

```bash
# Build all services
for service in auth-service patient-service billing-service analytics-service api-gateway; do
  cd $service
  ./mvnw clean package
  cd ..
done
```

#### 3. Run Services

**Terminal 1 - Auth Service:**
```bash
cd auth-service
export JWT_SECRET=5ZveJML9b0iaZQ2NT/sDUdUCcWaRhZ74ck1Rc6kHLh4=
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5001/db
java -jar target/auth-service-0.0.1-SNAPSHOT.jar
```

**Terminal 2 - Billing Service:**
```bash
cd billing-service
java -jar target/billing-service-0.0.1-SNAPSHOT.jar
```

**Terminal 3 - Analytics Service:**
```bash
cd analytics-service
java -jar target/analytics-service-0.0.1-SNAPSHOT.jar
```

**Terminal 4 - Patient Service:**
```bash
cd patient-service
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5000/db
java -jar target/patient-service-0.0.1-SNAPSHOT.jar
```

**Terminal 5 - API Gateway:**
```bash
cd api-gateway
export AUTH_SERVICE_URL=http://localhost:4005
java -jar target/api-gateway-0.0.1-SNAPSHOT.jar
```

### Running with Docker

#### 1. Create Docker Network

```bash
docker network create internal
```

#### 2. Build Docker Images

```bash
# Build all service images
for service in auth-service patient-service billing-service analytics-service api-gateway; do
  cd $service
  docker build -t $service:latest .
  cd ..
done
```

#### 3. Run Containers

**PostgreSQL Databases:**
```bash
# Patient Service Database
docker run -d \
  --name patient-service-db \
  --network internal \
  -e POSTGRES_USER=admin_user \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=db \
  -p 5000:5432 \
  postgres:latest

# Auth Service Database
docker run -d \
  --name auth-service-db \
  --network internal \
  -e POSTGRES_USER=admin_user \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=db \
  -p 5001:5432 \
  postgres:latest
```

**Kafka:**
```bash
docker run -d \
  --name kafka \
  --network internal \
  -p 9092:9092 \
  -p 9094:9094 \
  -e KAFKA_CFG_NODE_ID=0 \
  -e KAFKA_CFG_PROCESS_ROLES=controller,broker \
  -e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@kafka:9093 \
  -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093,EXTERNAL://:9094 \
  -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,EXTERNAL://localhost:9094 \
  -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,EXTERNAL:PLAINTEXT,PLAINTEXT:PLAINTEXT \
  -e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  bitnamilegacy/kafka:latest
```

**Auth Service:**
```bash
docker run -d \
  --name auth-service \
  --network internal \
  -p 4005:4005 \
  -e JWT_SECRET=5ZveJML9b0iaZQ2NT/sDUdUCcWaRhZ74ck1Rc6kHLh4= \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://auth-service-db:5432/db \
  -e SPRING_DATASOURCE_USERNAME=admin_user \
  -e SPRING_DATASOURCE_PASSWORD=password \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
  -e SPRING_SQL_INIT_MODE=always \
  auth-service:latest
```

**Billing Service:**
```bash
docker run -d \
  --name billing-service \
  --network internal \
  -p 4001:4001 \
  -p 9001:9001 \
  billing-service:latest
```

**Analytics Service:**
```bash
docker run -d \
  --name analytics-service \
  --network internal \
  -p 4002:4002 \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  analytics-service:latest
```

**Patient Service:**
```bash
docker run -d \
  --name patient-service \
  --network internal \
  -p 4000:4000 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://patient-service-db:5432/db \
  -e SPRING_DATASOURCE_USERNAME=admin_user \
  -e SPRING_DATASOURCE_PASSWORD=password \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
  -e SPRING_SQL_INIT_MODE=always \
  -e BILLING_SERVICE_ADDRESS=billing-service \
  -e BILLING_SERVICE_GRPC_PORT=9001 \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  patient-service:latest
```

**API Gateway:**
```bash
docker run -d \
  --name api-gateway \
  --network internal \
  -p 4004:4004 \
  -e AUTH_SERVICE_URL=http://auth-service:4005 \
  api-gateway:latest
```

### Using IntelliJ Docker Run Configurations

The project includes pre-configured IntelliJ run configurations in the `dockerFileConfig/` directory:
- `auth-service.run.xml` - Auth service with JWT configuration
- `patient-service-db.run.xml` - PostgreSQL database for patient service
- `kafka.run.xml` - Kafka broker
- `billing-service.run.xml` - Billing service
- `analytics-service.run.xml` - Analytics service
- `patient-service.run.xml` - Patient service
- `api-gateway.run.xml` - API Gateway

These can be imported into IntelliJ IDEA for easy container management.

## Testing the Application

### Authentication Flow

**1. Login to get JWT token:**
```http
POST http://localhost:4004/auth/login
Content-Type: application/json

{
  "email": "testuser@test.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**2. Validate token:**
```http
GET http://localhost:4004/auth/validate
Authorization: Bearer <your-token>
```

### Patient Service Testing (Through API Gateway)

**Get All Patients (requires JWT):**
```http
GET http://localhost:4004/api/patients
Authorization: Bearer <your-token>
```

**Create a Patient (requires JWT):**
```http
POST http://localhost:4004/api/patients
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "name": "John Smith",
  "email": "john.smith@example.com",
  "address": "123 Main St",
  "dateOfBirth": "1990-01-15",
  "registeredDate": "2025-01-15"
}
```

**Update a Patient (requires JWT):**
```http
PUT http://localhost:4004/api/patients/{id}
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "name": "John Smith Updated",
  "email": "john.smith@example.com",
  "address": "456 Oak Ave",
  "dateOfBirth": "1990-01-15"
}
```

**Delete a Patient (requires JWT):**
```http
DELETE http://localhost:4004/api/patients/{id}
Authorization: Bearer <your-token>
```

### Direct Service Testing (Bypassing Gateway)

**Patient Service Direct Access:**
```http
POST http://localhost:4000/patients
Content-Type: application/json

{
  "name": "Direct Test",
  "email": "direct@test.com",
  "address": "123 Test St",
  "dateOfBirth": "1990-01-15",
  "registeredDate": "2025-01-15"
}
```

### gRPC Testing

Use the sample request in `grpc-requests/billing-service/`:

```
GRPC localhost:9001/BillingService/CreateBillingAccount
Content-Type: application/json

{
  "patientId": "12334",
  "name": "John Doe",
  "email": "john.doe@example.com"
}
```

### Verifying Kafka Events

When a patient is created, check the analytics-service logs:
```bash
docker logs -f analytics-service
```

You should see:
```
Received Patient Event: [ PatientId=..., PatientName=..., PatientEmail=... ]
```

## API Documentation

OpenAPI/Swagger documentation is automatically generated and available at:

- **Patient Service Swagger UI:** `http://localhost:4000/swagger-ui.html`
- **Patient Service OpenAPI JSON:** `http://localhost:4000/v3/api-docs`
- **Auth Service Swagger UI:** `http://localhost:4005/swagger-ui.html`
- **Auth Service OpenAPI JSON:** `http://localhost:4005/v3/api-docs`

**Via API Gateway:**
- **Patient Service API Docs:** `http://localhost:4004/api-docs/patients`
- **Auth Service API Docs:** `http://localhost:4004/api-docs/auth`

## Database Configuration

### PostgreSQL (Production)

**Patient Service** uses PostgreSQL by default:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5000/db
spring.datasource.username=admin_user
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
```

**Auth Service** also uses PostgreSQL:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5001/db
spring.datasource.username=admin_user
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
```

### H2 Database (Testing)

To use in-memory H2 database for patient-service, uncomment the H2 configuration in `patient-service/src/main/resources/application.properties`:
```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.datasource.url=jdbc:h2:mem:testdb
# ... additional H2 configuration
```

Access H2 console at `http://localhost:4000/h2-console`

## Key Dependencies

### API Gateway Dependencies
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

### Authentication Dependencies
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### gRPC Dependencies
```xml
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty-shaded</artifactId>
    <version>1.69.0</version>
</dependency>
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-spring-boot-starter</artifactId>
    <version>3.1.0.RELEASE</version>
</dependency>
```

### Kafka Dependencies
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
    <version>3.3.0</version>
</dependency>
```

### Protocol Buffers
```xml
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>4.29.1</version>
</dependency>
```

## Security Features

### JWT Token Structure
- **Algorithm:** HS256 (HMAC with SHA-256)
- **Secret Key:** Base64-encoded 256-bit key
- **Expiration:** 10 hours from issuance
- **Claims:** Subject (email), Role, Issued At, Expiration

### API Gateway Security
- Custom JWT validation filter for protected routes
- WebClient-based token validation against Auth Service
- Automatic 401 Unauthorized response for invalid tokens
- Path-based security (auth endpoints are public, patient endpoints are protected)

### Password Security
- BCrypt hashing with random salt
- Minimum 8 character password requirement
- Secure password comparison

## Design Patterns & Best Practices

- **API Gateway Pattern** - Single entry point for all client requests
- **Authentication/Authorization** - Centralized JWT-based security
- **Repository Pattern** - Data access abstraction via Spring Data JPA
- **DTO Pattern** - Separation of internal models from API contracts
- **Mapper Pattern** - Manual DTO-Entity conversion
- **Service Layer** - Business logic encapsulation
- **Global Exception Handling** - Centralized error handling with `@ControllerAdvice`
- **Validation Groups** - Context-specific validation rules
- **Synchronous RPC** - gRPC for inter-service communication
- **Event-Driven Architecture** - Kafka for asynchronous messaging
- **Multi-Stage Docker Builds** - Optimized container images
- **Reactive Programming** - Spring WebFlux in API Gateway

## Request Flow Example

### Creating a Patient with Full Flow

1. **Client authenticates:**
   ```
   POST /auth/login → Auth Service
   Returns: JWT Token
   ```

2. **Client creates patient:**
   ```
   POST /api/patients (with JWT) → API Gateway
   → Validates JWT with Auth Service
   → Routes to Patient Service
   ```

3. **Patient Service processes:**
   ```
   - Validates request data
   - Checks email uniqueness
   - Saves patient to PostgreSQL
   - Calls Billing Service via gRPC
   - Publishes event to Kafka
   - Returns patient data
   ```

4. **Billing Service:**
   ```
   - Receives gRPC call
   - Creates billing account
   - Returns account details
   ```

5. **Analytics Service:**
   ```
   - Consumes Kafka event
   - Logs patient creation
   - Performs analytics processing
   ```

## Service Ports Summary

| Service | HTTP Port | Additional Ports | Purpose |
|---------|-----------|------------------|---------|
| API Gateway | 4004 | - | Entry point for all client requests |
| Auth Service | 4005 | - | JWT authentication and validation |
| Patient Service | 4000 | - | Patient CRUD operations |
| Billing Service | 4001 | 9001 (gRPC) | Billing account management |
| Analytics Service | 4002 | - | Event processing and analytics |
| PostgreSQL (Patient) | 5000 | - | Patient service database |
| PostgreSQL (Auth) | 5001 | - | Auth service database |
| Kafka | 9094 | 9092 (internal) | Event streaming |

## Monitoring & Logging

All services use SLF4J with Logback for logging:
- **Patient Service:** Logs REST requests, gRPC calls, and Kafka events
- **Billing Service:** Logs incoming gRPC requests
- **Analytics Service:** Logs consumed Kafka events
- **Auth Service:** Logs authentication attempts and token validation
- **API Gateway:** Logs routing decisions and validation failures

## Troubleshooting

### Common Issues

**API Gateway 401 Unauthorized:**
- Ensure JWT token is valid and not expired
- Check Authorization header format: `Bearer <token>`
- Verify Auth Service is running and accessible
- Check JWT_SECRET environment variable matches across services

**Kafka Connection Refused:**
- Ensure Kafka is running and accessible
- Check `SPRING_KAFKA_BOOTSTRAP_SERVERS` environment variable
- Verify network connectivity between services
- Wait for Kafka to fully start (can take 30-60 seconds)

**gRPC Connection Failed:**
- Ensure billing-service is running on port 9001
- Check `BILLING_SERVICE_ADDRESS` and `BILLING_SERVICE_GRPC_PORT` configuration
- Verify network connectivity between services

**Database Connection Failed:**
- Ensure PostgreSQL is running
- Verify database credentials
- Check database URL configuration
- Wait for PostgreSQL to fully start

**Proto Compilation Errors:**
- Run `mvn clean compile` to regenerate proto classes
- Ensure protobuf-maven-plugin is properly configured
- Check that proto files are in `src/main/proto/` directory

**Auth Service JWT Errors:**
- Verify JWT_SECRET is properly base64-encoded
- Ensure secret is at least 256 bits (32 characters when decoded)
- Check that the same secret is used across all services

## Environment Variables Reference

### API Gateway
- `AUTH_SERVICE_URL` - URL of the authentication service (default: http://localhost:4005)

### Auth Service
- `JWT_SECRET` - Base64-encoded secret key for JWT signing (required)
- `SPRING_DATASOURCE_URL` - PostgreSQL connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

### Patient Service
- `BILLING_SERVICE_ADDRESS` - Billing service hostname (default: localhost)
- `BILLING_SERVICE_GRPC_PORT` - Billing service gRPC port (default: 9001)
- `SPRING_KAFKA_BOOTSTRAP_SERVERS` - Kafka broker address (default: localhost:9092)
- `SPRING_DATASOURCE_URL` - PostgreSQL connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

### Analytics Service
- `SPRING_KAFKA_BOOTSTRAP_SERVERS` - Kafka broker address (default: localhost:9092)

## Sample Data

### Pre-loaded Users (Auth Service)
- **Email:** testuser@test.com
- **Password:** password123
- **Role:** ADMIN

### Pre-loaded Patients (Patient Service)
15 sample patients with UUIDs ranging from `123e4567-e89b-12d3-a456-426614174000` to `223e4567-e89b-12d3-a456-426614174014`


## License

This is a demonstration project showcasing microservices architecture patterns with Spring Boot, Spring Cloud Gateway, gRPC, Apache Kafka, and JWT authentication.