# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Run

```bash
# Start RabbitMQ (required before running the app)
cd docker && docker compose up -d

# Run the application
./mvnw spring-boot:run

# Build JAR
./mvnw clean package

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClassName
```

The app runs on **port 8877**. RabbitMQ management UI is at `http://localhost:15672` (guest/guest).

## Architecture

**Rabbithole** is a Spring Boot web UI for sending and receiving RabbitMQ messages. It supports queues, exchanges, and streams.

### Key components

- **`RabbitMQConnectionService`** — owns the RabbitMQ connection lifecycle. All other services call into this when connection parameters change. Thread-safe with synchronized methods.
- **`RabbitMQSender`** / **`RabbitMQReceiver`** — handle message I/O. Sender uses `RabbitTemplate`; Receiver handles both AMQP queues and RabbitMQ Streams (via the Stream client library). Both are refreshed by `ConnectionService` on reconnect.
- **`MessageController`** — MVC controller for `/send` and `/receive` form submissions.
- **`RabbitMQConnectionController`** — REST endpoints at `/connection/{connect,disconnect,status}` for dynamic broker config changes.
- **`EventController`** — SSE endpoint at `/events` that pushes connection status updates to the UI.

### Dynamic connections

Connection parameters (host, port, credentials) are **not fixed at startup** — users configure them through the connection page at runtime. When `/connection/connect` is called, `RabbitMQConnectionService` tears down the old connection and notifies `Sender` and `Receiver` to refresh.

### Stream vs. queue receiving

- **Queue receive:** drains all available messages in a non-blocking loop.
- **Stream receive:** attaches at a given offset (`first()` or numeric), collects for 5 seconds, then returns.

### Headers

Send forms accept comma-separated `key:value` pairs that become AMQP message headers. Stream sends also force `content-type: application/json`.

## Configuration

`src/main/resources/application.properties` sets defaults (host, ports, credentials). Docker Compose in `docker/` starts RabbitMQ 3.11 with the management, stream, and shovel plugins enabled.
