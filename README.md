# SpareLink Inventory Service

[![CI](https://github.com/tadiwanashe-mashongwa/inventory-service/actions/workflows/ci.yml/badge.svg)](https://github.com/tadiwanashe-mashongwa/inventory-service/actions/workflows/ci.yml)
[![JaCoCo coverage](.github/badges/jacoco.svg)](target/site/jacoco/index.html)

The Inventory Service manages spare-part stock for the SpareLink automotive platform. It exposes secured stock operations and consumes order events to reserve stock reliably.

## Capabilities

- Add stock and record breakage deductions.
- Return the available quantity for a part.
- Reserve stock when the Order Service publishes an `order-created` Kafka event.
- Prevent duplicate reservations for the same order and part.
- Persist stock levels and reservations in PostgreSQL with Flyway-managed schema migrations and optimistic locking.
- Enforce Keycloak JWT roles: `CUSTOMER` and `ADMIN`.

## Architecture

```mermaid
flowchart LR
    Client[Customer or admin client] -->|JWT REST request| API[Inventory Service]
    API --> DB[(Inventory PostgreSQL)]
    Order[Order Service] -->|order-created| Kafka[(Kafka)]
    Kafka --> Listener[Order event listener]
    Listener --> API
    Keycloak[Keycloak] -->|JWT issuer| API
```

## Stock reservation flow

```mermaid
sequenceDiagram
    participant O as Order Service
    participant K as Kafka
    participant I as Inventory Service
    participant D as PostgreSQL

    O->>K: order-created(orderId, items)
    K->>I: deliver event
    I->>D: check reservation(orderId, partId)
    alt Reservation does not exist and stock is available
        I->>D: decrease available / increase reserved
        I->>D: persist reservation
    else Existing reservation
        I-->>I: ignore duplicate event
    else Insufficient stock
        I-->>I: log failed reservation
    end
```

## API and security

| Endpoint | Role | Purpose |
| --- | --- | --- |
| `GET /api/inventory/stock/{partId}` | `CUSTOMER` or `ADMIN` | Get available stock |
| `POST /api/inventory/stock?partId={id}&quantity={n}` | `ADMIN` | Add stock |
| `POST /api/inventory/breakage?partId={id}&quantity={n}` | `ADMIN` | Deduct damaged stock |
| `GET /actuator/health` | Public | Health probe |
| `/swagger-ui/index.html` | Public | Interactive API documentation |
| `/v3/api-docs` | Public | OpenAPI specification |

JWTs are validated against `KEYCLOAK_ISSUER_URI`. The default issuer is `http://localhost:8080/realms/sparelink`.

## Database model

```mermaid
erDiagram
    STOCK_LEVELS {
        bigint id PK
        varchar part_id UK
        integer available_quantity
        integer reserved_quantity
        bigint version
    }
    RESERVATIONS {
        bigint id PK
        varchar order_id
        varchar part_id
        integer quantity
        varchar status
    }
    STOCK_LEVELS ||--o{ RESERVATIONS : reserves
```

Flyway migration `V1__create_inventory_schema.sql` owns the database schema. Hibernate validates it rather than creating tables.

## Run locally

Start the service and infrastructure with Docker:

```powershell
docker compose up --build
```

The application is exposed at `http://localhost:8082`; PostgreSQL at `localhost:5432`; Kafka at `localhost:9092`.

## Testing and coverage

The test suite covers unit, controller/security, global exception, Spring integration, Flyway, PostgreSQL Testcontainers, and Kafka Testcontainers scenarios.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
.\mvnw.cmd test
```

GitHub Actions runs this suite for every pull request and push to `main`, uploads the JaCoCo report, and refreshes the coverage badge after successful main-branch builds.
