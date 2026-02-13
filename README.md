**Distributed Notification System**

This repository contains a sample microservice-based distributed notification system. It demonstrates a scalable design using Kafka for messaging, PostgreSQL for persistence, and multiple services (email, SMS, push, user preferences, dispatcher, API gateway) that collaborate to deliver notifications.

**Architecture**

- **Messaging:** Kafka is used for topics and async communication between producers and consumers.
- **Services:** Individual services live in their folders: [api-gateway](api-gateway), [email-service](email-service), [sms-service](sms-service), [push-service](push-service), [notification-dispatcher](notification-dispatcher), [user-preferences-service](user-preferences-service), plus shared modules [common-contract](common-contract) and [notification-proto](notification-proto).
- **Persistence:** PostgreSQL used for durable data (see `postgres-data` for sample data/config).
- **Observability:** OpenTelemetry collector config is provided in [otel-collector-config.yaml](otel-collector-config.yaml).

**Repository Layout**

- **Root:** orchestration and helper files such as [docker-compose.yml](docker-compose.yml) and documentation like [kafka-topic-creation.md](kafka-topic-creation.md).
- **Service folders:** each microservice is a Maven-based Java project with its own `pom.xml` and Dockerfile.

**Prerequisites**

- **Docker & Docker Compose:** to run the whole stack via `docker-compose`.
- **Java 11+ and Maven:** to build services locally (`mvn`).
- **Kafka & Zookeeper (optional):** if running services without docker-compose and you want to manage Kafka yourself.

**Quick Start (Docker Compose)**

1. From the repository root, start the full stack:

   `docker-compose up --build`

2. Services will be available on the ports defined in the compose file. Check logs with `docker-compose logs -f`.

3. To stop and remove containers:

   `docker-compose down`

**Build Services Locally**

- Build a specific service (example: `email-service`):

  `cd email-service && ./mvnw package`

- Build all modules from the repo root:

  `./mvnw clean package -DskipTests`

**Kafka Topics**

- Topic creation instructions and recommended topic names are in [kafka-topic-creation.md](kafka-topic-creation.md). Use that to provision topics before starting producers/consumers if not using the provided compose orchestration.

**Configuration & Secrets**

- Each service reads configuration from environment variables and/or `application.properties` in `src/main/resources`.
- When running in Docker, set environment variables in `docker-compose.yml` or via a `.env` file.

**Database**

- A sample PostgreSQL data directory is included under `postgres-data` for reference. The DB configuration can be adjusted in `docker-compose.yml` or service environment variables.

**Observability**

- The OpenTelemetry collector configuration is in [otel-collector-config.yaml](otel-collector-config.yaml). Services are instrumented to export traces/metrics to the collector.

**Development Tips**

- Run and debug a single service locally by building with Maven and running from your IDE.
- Use the API gateway to route requests to downstream services when testing end-to-end.

**Useful Commands**

- Build a service: `./mvnw -f email-service/pom.xml package`
- Run compose in background: `docker-compose up -d`
- View logs for a single service: `docker-compose logs -f email-service`

**References & Links**

- Kafka topic details: [kafka-topic-creation.md](kafka-topic-creation.md)
- OpenTelemetry collector config: [otel-collector-config.yaml](otel-collector-config.yaml)

---
