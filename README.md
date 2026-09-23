# MedStack Services

A Spring Boot microservices playground built around a small healthcare domain (patients, billing, auth) to explore how REST, gRPC, and Kafka fit together behind a single API Gateway.

## What's in here

Five services, each doing one job:

| Service | Job | Talks over |
|---|---|---|
| API Gateway | Front door for every client request; checks JWTs before forwarding | REST |
| Auth Service | Issues and validates JWTs | REST |
| Patient Service | Owns patient CRUD; kicks off billing + analytics on writes | REST in, gRPC + Kafka out |
| Billing Service | Creates a billing account per patient | gRPC |
| Analytics Service | Listens for patient events and logs/analyzes them | Kafka |

### How a request moves through the system

```
                    client
                       │
                       ▼
             ┌───────────────────┐
             │    API Gateway    │   :4004
             │  (JWT check here) │
             └─────────┬─────────┘
                        │
          ┌─────────────┴─────────────┐
          ▼                            ▼
  ┌───────────────┐           ┌────────────────┐
  │  Auth Service  │           │ Patient Service │  :4000
  │     :4005      │           └────────┬─────────┘
  └───────────────┘                     │
                              ┌──────────┴──────────┐
                              │ gRPC              Kafka
                              ▼                      ▼
                     ┌────────────────┐     ┌──────────────────┐
                     │ Billing Service │     │ Analytics Service │
                     │  :4001 / :9001  │     │       :4002       │
                     └────────────────┘     └──────────────────┘
```

## Stack

- **Java 21 / Spring Boot 3.4.x** across all services, built with Maven (wrapper included, no local Maven needed)
- **Spring Cloud Gateway** for routing + a custom JWT filter at the edge
- **Spring Security + JJWT + BCrypt** for auth (HS256 tokens, 10h expiry)
- **gRPC** (`grpc-spring-boot-starter`, protobuf) between patient-service and billing-service
- **Apache Kafka** for the patient → analytics event pipe
- **Spring Data JPA + PostgreSQL** for persistence (H2 works too if you flip a config flag)
- **springdoc-openapi** for auto-generated Swagger docs
- Everything is Dockerized with its own Dockerfile; `dockerFileConfig/` has ready-made IntelliJ run configs if you use that IDE

## Layout

```
.
├── api-gateway/         routing + JWT validation filter
├── auth-service/        login/validate endpoints, JWT issuing, user store
├── patient-service/     the "main" service: REST CRUD, gRPC client, Kafka producer
├── billing-service/     gRPC server, creates billing accounts
├── analytics-service/   Kafka consumer, logs patient events
├── api-requests/        .http files for exercising REST endpoints
├── grpc-requests/       .http-style file for hitting billing-service over gRPC
└── dockerFileConfig/    IntelliJ Docker run configs per service
```

Inside each service the package layout is the usual Spring split: `controller` / `service` / `repository` / `model` / `dto`, plus `grpc` or `kafka` packages where relevant, and `proto/` for the `.proto` contracts.

## Service details

**API Gateway (`:4004`)** — routes `/auth/**` to auth-service and `/api/patients/**` to patient-service, stripping the prefix and running a custom `JwtValidation` filter (implemented with `WebClient`, calling back into auth-service) on the patient routes. Bad/missing tokens get a global-exception-handled 401.

**Auth Service (`:4005`)** — `POST /login` takes an email/password and returns a JWT; `GET /validate` checks one. Passwords are BCrypt-hashed, tokens are HS256 signed with a base64 secret shared via env var, and there's a seeded user (`testuser@test.com` / `password123`, role ADMIN) for local testing.

**Patient Service (`:4000`)** — the CRUD core: `GET/POST/PUT/DELETE /patients`. UUID primary keys, email-uniqueness checks, separate validation groups for create vs. update, and Swagger at `/swagger-ui.html`. On create, it calls billing-service over gRPC and fires a patient event onto Kafka. Comes with 15 seeded patients.

**Billing Service (`:4001` HTTP / `:9001` gRPC)** — single RPC, `CreateBillingAccount(BillingRequest) → BillingResponse`. Deliberately thin.

**Analytics Service (`:4002`)** — subscribes to the `patient` topic (consumer group `analytics-service`), deserializes the protobuf event, and logs it. Stands in for whatever real analytics processing would go here.

### The two contracts

gRPC (`billing_service.proto`):
```protobuf
service BillingService {
  rpc CreateBillingAccount (BillingRequest) returns (BillingResponse);
}
message BillingRequest  { string patientId = 1; string name = 2; string email = 3; }
message BillingResponse { string accountId = 1; string status = 2; }
```

Kafka event (`patient_event.proto`):
```protobuf
message PatientEvent {
  string patientId = 1;
  string name = 2;
  string email = 3;
  string event_type = 4;
}
```

## Running it

### Prerequisites
Java 21, Docker (for Postgres/Kafka, or run the services in containers too), and that's it — Maven comes via the wrapper.

### Quickest path: spin up infra with Docker, run services with `java -jar`

**1. Postgres x2 + Kafka:**
```bash
docker run -d --name patient-service-db -e POSTGRES_USER=admin_user -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=db -p 5000:5432 postgres:latest

docker run -d --name auth-service-db -e POSTGRES_USER=admin_user -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=db -p 5001:5432 postgres:latest

docker run -d --name kafka -p 9092:9092 -p 9094:9094 \
  -e KAFKA_CFG_NODE_ID=0 -e KAFKA_CFG_PROCESS_ROLES=controller,broker \
  -e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@kafka:9093 \
  -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093,EXTERNAL://:9094 \
  -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,EXTERNAL://localhost:9094 \
  -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,EXTERNAL:PLAINTEXT,PLAINTEXT:PLAINTEXT \
  -e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  bitnamilegacy/kafka:latest
```

**2. Build every service:**
```bash
for service in auth-service patient-service billing-service analytics-service api-gateway; do
  (cd $service && ./mvnw clean package)
done
```

**3. Run each one (separate terminals), roughly in this order — auth/billing/analytics can start anytime, patient-service needs billing+kafka up, gateway needs auth up:**
```bash
# auth-service
export JWT_SECRET=5ZveJML9b0iaZQ2NT/sDUdUCcWaRhZ74ck1Rc6kHLh4=
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5001/db
java -jar auth-service/target/auth-service-0.0.1-SNAPSHOT.jar

# billing-service
java -jar billing-service/target/billing-service-0.0.1-SNAPSHOT.jar

# analytics-service
java -jar analytics-service/target/analytics-service-0.0.1-SNAPSHOT.jar

# patient-service
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5000/db
java -jar patient-service/target/patient-service-0.0.1-SNAPSHOT.jar

# api-gateway
export AUTH_SERVICE_URL=http://localhost:4005
java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar
```

### Full Docker path

```bash
docker network create internal
```

Build each image:
```bash
for service in auth-service patient-service billing-service analytics-service api-gateway; do
  (cd $service && docker build -t $service:latest .)
done
```

Bring up infra on the same network (same `docker run` commands as above, plus `--network internal`), then the services:

```bash
docker run -d --name auth-service --network internal -p 4005:4005 \
  -e JWT_SECRET=5ZveJML9b0iaZQ2NT/sDUdUCcWaRhZ74ck1Rc6kHLh4= \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://auth-service-db:5432/db \
  -e SPRING_DATASOURCE_USERNAME=admin_user -e SPRING_DATASOURCE_PASSWORD=password \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO=update -e SPRING_SQL_INIT_MODE=always \
  auth-service:latest

docker run -d --name billing-service --network internal -p 4001:4001 -p 9001:9001 billing-service:latest

docker run -d --name analytics-service --network internal -p 4002:4002 \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 analytics-service:latest

docker run -d --name patient-service --network internal -p 4000:4000 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://patient-service-db:5432/db \
  -e SPRING_DATASOURCE_USERNAME=admin_user -e SPRING_DATASOURCE_PASSWORD=password \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO=update -e SPRING_SQL_INIT_MODE=always \
  -e BILLING_SERVICE_ADDRESS=billing-service -e BILLING_SERVICE_GRPC_PORT=9001 \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  patient-service:latest

docker run -d --name api-gateway --network internal -p 4004:4004 \
  -e AUTH_SERVICE_URL=http://auth-service:4005 api-gateway:latest
```

If you're on IntelliJ, skip typing all that and import the run configs from `dockerFileConfig/` instead.

## Trying it out

Log in, use the token everywhere else:
```http
POST http://localhost:4004/auth/login
Content-Type: application/json

{ "email": "testuser@test.com", "password": "password123" }
```

Then, through the gateway (`Authorization: Bearer <token>` on all of these):
```http
GET    http://localhost:4004/api/patients
POST   http://localhost:4004/api/patients
PUT    http://localhost:4004/api/patients/{id}
DELETE http://localhost:4004/api/patients/{id}
```

More worked examples live in `api-requests/` (REST) and `grpc-requests/` (billing gRPC call). Creating a patient should also produce a billing account behind the scenes and a log line in analytics-service — check with `docker logs -f analytics-service` and look for `Received Patient Event: [...]`.

Swagger, if you'd rather click around:
- `http://localhost:4000/swagger-ui.html` (patient-service)
- `http://localhost:4005/swagger-ui.html` (auth-service)
- or via the gateway: `http://localhost:4004/api-docs/patients` / `/api-docs/auth`

## Ports at a glance

| Service | Port(s) |
|---|---|
| API Gateway | 4004 |
| Auth Service | 4005 |
| Patient Service | 4000 |
| Billing Service | 4001 (HTTP), 9001 (gRPC) |
| Analytics Service | 4002 |
| Patient Postgres | 5000 |
| Auth Postgres | 5001 |
| Kafka | 9094 external / 9092 internal |

## Env vars that matter

- `JWT_SECRET` (auth-service, required) — base64, must be identical everywhere it's checked
- `AUTH_SERVICE_URL` (api-gateway) — where to send validation calls
- `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` (auth-service, patient-service)
- `BILLING_SERVICE_ADDRESS` / `BILLING_SERVICE_GRPC_PORT` (patient-service)
- `SPRING_KAFKA_BOOTSTRAP_SERVERS` (patient-service, analytics-service)

## If something's not working

- **401 from the gateway** — token expired, wrong `Authorization: Bearer` format, auth-service down, or `JWT_SECRET` mismatched between auth-service and whatever's validating.
- **Kafka connection refused** — give it 30-60s to actually finish starting, then double check `SPRING_KAFKA_BOOTSTRAP_SERVERS`.
- **gRPC call failing** — billing-service not up on 9001, or `BILLING_SERVICE_ADDRESS`/`BILLING_SERVICE_GRPC_PORT` pointing at the wrong host.
- **DB connection errors** — Postgres not ready yet, or credentials/URL don't match what you passed to the container.
- **Proto classes missing/stale** — `mvn clean compile` to regenerate from `src/main/proto/`.

## Notes on the design

Nothing exotic — standard Spring layering (controller/service/repository), DTOs at the API boundary with manual mappers, `@ControllerAdvice` for centralized exception handling, validation groups so create/update rules don't collide. The interesting part is really the three communication styles side by side: synchronous REST at the edge, synchronous gRPC between two backend services, and async Kafka fan-out to a consumer that doesn't block anything upstream.

This is a learning/demo project, not a production system — secrets are hardcoded in the docker run examples above for convenience, don't ship that part.
