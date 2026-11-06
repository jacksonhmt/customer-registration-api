# 👥 Customer Registration API

A REST API for registering and managing customers and their addresses. It is built with Java, Spring Boot, Spring Data JPA, PostgreSQL, Flyway, and Lombok.

## 📚 Contents

- [Features](#-features)
- [Requirements](#-requirements)
- [Run locally](#-run-locally)
- [Configuration](#-configuration)
- [API](#-api)
- [Errors](#-errors)
- [API documentation](#-api-documentation)
- [Tests](#-tests)
- [Database migration](#-database-migration)

## ✨ Features

- Create, retrieve, list, update, and soft-delete customers.
- Store one address per customer; an address may be omitted.
- Validate Brazilian CPF, phone numbers, ZIP codes (CEP), and state abbreviations (UF).
- Prevent duplicate CPF and email registrations.
- Paginate customer listings.
- Expose interactive OpenAPI documentation.
- Run automated tests against an in-memory H2 database.

> 🔒 **Privacy:** CPF is required when creating or updating a customer, but is deliberately not included in API responses to minimize exposure of personal data.

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

The API is available at `http://localhost:8080`. Flyway applies the database schema migration at startup, and Hibernate validates the schema.

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

`PUT /api/v1/customers/{id}` performs a complete update and expects the same request fields as registration. `DELETE /api/v1/customers/{id}` marks the customer as deleted; it does not physically remove the database record. Deleted customers are no longer returned by retrieval, listing, update, or delete endpoints.

## 🚦 Errors

Errors use the same envelope shape with `success: false` and an `errors` array:

```json
{
  "success": false,
  "message": "Validation error.",
  "errors": [
    {
      "field": "email",
      "message": "The provided email is invalid."
    }
  ]
}
```

Common status codes:

| Status | Meaning |
| --- | --- |
| `400 Bad Request` | Invalid request body, field, or pagination parameter |
| `404 Not Found` | Customer or resource does not exist |
| `409 Conflict` | CPF or email is already registered |
| `415 Unsupported Media Type` | Request is not sent as JSON |
| `500 Internal Server Error` | Unexpected server error |

## 📖 API documentation

With the application running, open the Swagger UI at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). The OpenAPI specification is available at [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs).

## 🧪 Tests

Run the test suite with:

```bash
./mvnw test
```

Tests use an in-memory H2 database per Spring test context and do not require Docker or PostgreSQL.

## 🗄️ Database migration

The initial schema is defined in `src/main/resources/db/migration/V1__create_customer_and_address.sql`. Flyway runs this migration automatically on application startup.
