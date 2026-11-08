# 👥 Customer Registration API

A REST API for registering and managing customers and their addresses. It is built with Java, Spring Boot, Spring Data JPA, PostgreSQL, Flyway, and Lombok.

## 📚 Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Requirements](#-requirements)
- [Run locally](#-run-locally)
- [Configuration](#-configuration)
- [API](#-api)
- [Errors](#-errors)
- [Internationalization](#-internationalization)
- [API documentation](#-api-documentation)
- [Tests](#-tests)
- [Continuous integration](#-continuous-integration)
- [Database migration](#-database-migration)

## ✨ Features

- Create, retrieve, list, update, and delete customers.
- Store one address per customer; an address may be omitted.
- Validate Brazilian CPF, phone numbers, ZIP codes (CEP), and state abbreviations (UF).
- Prevent duplicate CPF and email registrations.
- Paginate customer listings.
- Expose interactive OpenAPI documentation.
- Run automated tests against an in-memory H2 database.

> 🔒 **Privacy:** CPF is required when creating or updating a customer, but is deliberately not included in API responses to minimize exposure of personal data.

## 🏗️ Architecture

The application uses a layered Spring Boot architecture:

```text
HTTP request
    ↓
CustomerController → request DTO validation
    ↓
CustomerServiceImpl → business rules and transaction boundaries
    ↓
CustomerMapper ↔ Customer / Address entities
    ↓
CustomerRepository → Spring Data JPA → PostgreSQL
```

- **API layer:** `CustomerController` exposes the customer endpoints and accepts validated request DTOs. `GlobalExceptionHandler` converts validation and application exceptions into the common API error response.
- **Service and mapping layers:** `CustomerServiceImpl` enforces duplicate CPF/email checks and wraps writes in transactions. `CustomerMapper` normalizes Brazilian identifiers and contact data, maps requests to entities, and creates response DTOs that omit CPF.
- **Persistence layer:** `CustomerRepository` provides Spring Data JPA queries. Flyway creates the schema from `src/main/resources/db/migration`; `Customer` has a one-to-one relationship with `Address`.
- **Messages and validation:** all user-facing texts live in `messages*.properties` (pt-BR, en-US). `CustomerMessages`, `AddressMessages` and `GeneralMessages` hold the keys, `MessageService` resolves them for the request locale, `InternationalizationConfig` selects the locale from `Accept-Language`, and the `enums` package holds `ErrorCode` (stable error codes) and `Gender`.
- **Tests:** automated tests use an in-memory H2 database and cover API, persistence, service, entity, and validation behavior.

The schema includes a `deleted_at` column and read queries exclude rows where it is set. However, the current delete service calls `repository.delete`, which physically deletes the customer (and its cascaded address); it does not set `deleted_at`.

## 🧰 Requirements

- Java 27
- Docker with Docker Compose (for the PostgreSQL setup below)

The included Maven Wrapper downloads and runs Maven; a separate Maven installation is not required.

Lombok generates accessors during compilation. Enable annotation processing and Lombok support in your IDE if generated methods are not recognized.

## 🚀 Run locally

1. Set `DB_PASSWORD` in your environment to the password for your PostgreSQL user. For example, on macOS or Linux:

   ```bash
   export DB_PASSWORD='<your-database-password>'
   ```

2. Start PostgreSQL if you don't already have a container listening on port `5432`:

   ```bash
   docker compose up -d
   ```

   The compose file starts PostgreSQL 17 with database `loja` and user `postgres`. If you already started `postgres-loja` with `docker run`, leave it running and skip this step; starting both containers would conflict on port `5432`.

3. Start the API from the same environment:

   ```bash
   ./mvnw spring-boot:run
   ```

   On Windows, set `DB_PASSWORD` in your shell and use `mvnw.cmd spring-boot:run`.

The API is available at `http://localhost:8080`. Flyway is disabled by default for an existing database; Hibernate validates the schema without creating or changing tables. For a new empty database, enable Flyway as described in [Database migration](#-database-migration).

To stop the database:

```bash
docker compose down
```

This command affects only the Compose-managed container, not a container started separately with `docker run`.

> ⚠️ **Data loss:** The following command also removes the persisted database volume.

```bash
docker compose down -v
```

## ⚙️ Configuration

The following environment variables can override the local database settings:

| Variable | Default | Description |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/loja` | JDBC URL for PostgreSQL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | Required | Database password; use the same value for the application and container |
| `DB_NAME` | `loja` | Database name used by Docker Compose |
| `SPRING_FLYWAY_ENABLED` | `false` | Enable Flyway migrations at startup |

Set the same database credentials for the application and the PostgreSQL container. For example:

```bash
export DB_PASSWORD='<your-database-password>'
DB_NAME=customers DB_USERNAME=app docker compose up -d
DB_URL=jdbc:postgresql://localhost:5432/customers DB_USERNAME=app ./mvnw spring-boot:run
```

PostgreSQL initialization settings apply only when the Compose volume is empty. Changing these variables does not update an existing database or user. A volume created with PostgreSQL 16 also requires a database upgrade or migration before it can be used with PostgreSQL 17; back up any data before changing versions.

## 🔌 API

All customer endpoints are under `/api/v1/customers` and consume/produce JSON.

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| `POST` | `/api/v1/customers` | Register a customer | `201 Created` |
| `GET` | `/api/v1/customers` | List customers | `200 OK` |
| `GET` | `/api/v1/customers/{id}` | Retrieve a customer | `200 OK` |
| `PUT` | `/api/v1/customers/{id}` | Replace customer data | `200 OK` |
| `DELETE` | `/api/v1/customers/{id}` | Soft-delete a customer | `200 OK` |

### Register a customer

```bash
curl -X POST http://localhost:8080/api/v1/customers \
  -H 'Content-Type: application/json' \
  -d '{
    "fullName": "Maria da Silva",
    "cpf": "529.982.247-25",
    "email": "maria@example.com",
    "phoneNumber": "(11) 99999-9999",
    "additionalPhone": null,
    "birthDate": "1990-05-20",
    "gender": "FEMALE",
    "address": {
      "zipCode": "01310-100",
      "street": "Avenida Paulista",
      "number": "1000",
      "complement": "Apt 12",
      "neighborhood": "Bela Vista",
      "city": "São Paulo",
      "state": "SP"
    }
  }'
```

`gender` is optional and accepts `MALE`, `FEMALE`, `OTHER`, or `PREFER_NOT_TO_SAY`. `address` and `additionalPhone` are optional. Dates use the `YYYY-MM-DD` format. CPF, phone, and CEP accept standard Brazilian formatting; the service normalizes CPF and email for storage and duplicate checks.

Successful responses use a common envelope:

```json
{
  "success": true,
  "message": "Customer successfully registered.",
  "data": {
    "id": 1,
    "fullName": "Maria da Silva",
    "email": "maria@example.com",
    "phoneNumber": "11999999999",
    "birthDate": "1990-05-20",
    "gender": "FEMALE",
    "address": {
      "zipCode": "01310100",
      "street": "Avenida Paulista",
      "number": "1000",
      "complement": "Apt 12",
      "neighborhood": "Bela Vista",
      "city": "São Paulo",
      "state": "SP"
    },
    "createdAt": "2026-01-01T12:00:00"
  }
}
```

Fields with null values may be omitted from responses.

### List customers

```bash
curl 'http://localhost:8080/api/v1/customers?page=0&size=20'
```

`page` is zero-based and defaults to `0`; `size` defaults to `20` and must be between `1` and `100`. The response `data` contains `content`, `page`, `size`, `totalElements`, and `totalPages`. Soft-deleted customers are excluded.

### Update and delete

`PUT /api/v1/customers/{id}` performs a complete update and expects the same request fields as registration. `DELETE /api/v1/customers/{id}` physically removes the customer and its associated address from the database.

## 🚦 Errors

Errors use `ApiErrorResponseDTO`. `code` is stable and language-independent; only `message` (and field messages) are translated. `errors` appears only when field details exist.

```json
{
  "success": false,
  "code": "CUSTOMER_NOT_FOUND",
  "message": "Customer not found.",
  "path": "/api/v1/customers/999"
}
```

Validation error (`400`):

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "Validation failed.",
  "path": "/api/v1/customers",
  "errors": [{ "field": "fullName", "message": "Full name is required." }]
}
```

Error codes: `CUSTOMER_NOT_FOUND`, `CUSTOMER_ALREADY_EXISTS`, `VALIDATION_ERROR`, `INVALID_REQUEST`, `RESOURCE_NOT_FOUND`, `METHOD_NOT_ALLOWED`, `UNSUPPORTED_MEDIA_TYPE`, `INTERNAL_SERVER_ERROR`. Unexpected errors never expose stack traces or internal details.

Common status codes:

| Status | Meaning |
| --- | --- |
| `400 Bad Request` | Invalid request body, field, or pagination parameter |
| `404 Not Found` | Customer or resource does not exist |
| `409 Conflict` | CPF or email is already registered |
| `415 Unsupported Media Type` | Request is not sent as JSON |
| `500 Internal Server Error` | Unexpected server error |

## 🌍 Internationalization

- **Supported languages:** Portuguese (Brazil) `pt-BR` and English (US) `en-US`.
- **Default:** `pt-BR` (used when the header is missing or the language is unsupported, e.g. `fr-FR` or `en-GB`).
- **Selection:** the `Accept-Language` header, via Spring's `AcceptHeaderLocaleResolver`. Lists such as `es-ES,en-US;q=0.8` pick the first supported language. A bare `en` is not matched; send `en-US`.

```bash
curl -H "Accept-Language: pt-BR" localhost:8080/api/v1/customers/999
# {"success":false,"code":"CUSTOMER_NOT_FOUND","message":"Cliente não encontrado.","path":"/api/v1/customers/999"}

curl -H "Accept-Language: en-US" localhost:8080/api/v1/customers/999
# {"success":false,"code":"CUSTOMER_NOT_FOUND","message":"Customer not found.","path":"/api/v1/customers/999"}
```

Success responses use `ApiResponseDTO<T>`: `{ "success": true, "message": "...", "data": ... }`.

**Add a message key:** add the same key to `messages.properties`, `messages_pt_BR.properties` and `messages_en_US.properties` (a test checks they match), then reference it from `CustomerMessages`, `AddressMessages` or `GeneralMessages` (use `{key}` form in annotations, `MessageService.getMessage(key)` in code).

**Add a language:** create `messages_<lang>_<COUNTRY>.properties` with all keys (UTF-8) and add the locale to `InternationalizationConfig`.

## 📖 API documentation

With the application running, open the Swagger UI at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). The OpenAPI specification is available at [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs).

## 🧪 Tests

Run the test suite with:

```bash
./mvnw test
```

Tests use an in-memory H2 database per Spring test context and do not require Docker or PostgreSQL.

## 🔄 Continuous integration

The GitHub Actions workflow in `.github/workflows/ci.yml` runs on pushes, pull requests, and manual dispatch. It uses Java 27 and the Maven Wrapper to compile, run tests, package the application, and execute Maven's `verify` phase. Tests use the H2 test profile, so CI does not need PostgreSQL credentials. Test reports are uploaded when available.

After the backend build succeeds, CI calls the reusable workflow `.github/workflows/tests.yml` on the `main` branch of [`customer-registration-api-test`](https://github.com/jacksonhmt/customer-registration-api-test), passing the triggering backend commit as `backend_ref`. That workflow must declare a required string input named `backend_ref` under `workflow_call` and check out that exact backend revision. For pull requests, GitHub normally supplies the synthetic merge commit as `github.sha`.

The reusable workflow must be present on the automation repository's `main` branch and accessible to this repository before CI can run successfully. If either repository is private, configure GitHub Actions access and checkout credentials as needed. Its automation environment should use a temporary PostgreSQL database and enable Flyway there; it must not connect to a developer's database. The acceptance tests must match the API response envelope and must not expect CPF in the response.

## 🗄️ Database migration

The initial schema is defined in `src/main/resources/db/migration/V1__create_customer_and_address.sql`. Flyway is disabled by default so an existing database is not migrated at startup. Hibernate still validates that its schema matches the entities.

For a new, empty database, enable Flyway when starting the API:

```bash
SPRING_FLYWAY_ENABLED=true ./mvnw spring-boot:run
```

Enabling Flyway on an existing database requires its migration history to be reconciled first. Back up the database and verify the schema before baselining; Flyway's baseline records a version but does not update tables. Future migrations will not run while Flyway is disabled. The test profile enables Flyway for its separate in-memory database.
