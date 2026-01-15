# Event-Driven Notification System

A learning project demonstrating event-driven architecture with Spring Boot, Kafka, and React.

## Architecture

```
┌─────────────────┐     ┌─────────────────────────────────────┐     ┌─────────────────┐
│ External Topics │     │           Event Router              │     │    Outbound     │
│                 │     │  ┌─────────┐    ┌────────────────┐  │     │                 │
│  orders.events  │────▶│  │ Listener │──▶│ Routing Rules  │──│────▶│ alerts topic    │
│  payments.events│     │  └─────────┘    │                │  │     │ Frontend (SSE)  │
│  inventory.event│     │                 │ if X then Y    │  │     │ External APIs   │
│                 │     │                 └────────────────┘  │     │ Event Log       │
└─────────────────┘     └─────────────────────────────────────┘     └─────────────────┘
```

- **Frontend**: React 18 + Vite + TypeScript
- **Backend**: Spring Boot 3 + Java 21 + Spring Kafka + Event Router
- **Message Broker**: Apache Kafka (via Docker)

## Prerequisites

- **Docker** and **Docker Compose** - for running Kafka
- **Java 21** - for the Spring Boot backend
- **Node.js 18+** and **npm** - for the React frontend

## Quick Start

### 1. Start Kafka

```bash
docker compose up -d
```

This starts:
- Zookeeper (port 2181)
- Kafka broker (port 29092 for host access)
- Kafka UI (port 8080) - optional web interface

Verify Kafka is running:
```bash
docker compose ps
```

### 2. Start the Backend

```bash
cd backend
./gradlew bootRun
```

The backend runs on http://localhost:8081

Verify it's healthy:
```bash
curl http://localhost:8081/actuator/health
```

### 3. Start the Frontend

```bash
cd frontend
npm install   # first time only
npm run dev
```

The frontend runs on http://localhost:5173

## Usage

1. Open http://localhost:5173 in your browser
2. Fill in the notification form (User ID, Type, Message)
3. Click "Send Notification"
4. Watch the notification appear in real-time on the right panel

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/notifications` | Create a new notification |
| GET | `/api/notifications` | Get all notifications |
| GET | `/api/notifications/stream` | SSE stream for real-time updates |

### Example: Send a notification via curl

```bash
curl -X POST http://localhost:8081/api/notifications \
  -H "Content-Type: application/json" \
  -d '{"userId": "user123", "type": "info", "message": "Hello from curl!"}'
```

## Project Structure

```
notification-system/
├── docker-compose.yml          # Kafka infrastructure
├── backend/
│   ├── build.gradle.kts        # Gradle build config
│   └── src/main/
│       ├── java/com/example/notifications/
│       │   ├── NotificationServiceApplication.java
│       │   ├── Notification.java
│       │   ├── NotificationRequest.java
│       │   ├── NotificationController.java
│       │   ├── KafkaProducerService.java
│       │   └── KafkaConsumerService.java
│       └── resources/
│           └── application.yml
└── frontend/
    ├── package.json
    └── src/
        ├── App.tsx
        ├── types.ts
        └── components/
            ├── NotificationForm.tsx
            └── NotificationList.tsx
```

## Event Router

The backend includes an Event Router that listens to external Kafka topics and reacts based on configurable rules.

### Simulating External Events

Use the CLI producer to send test events:

```bash
cd backend

# High-value order (triggers alert)
./gradlew sendEvent --args="orders.events order.created '{\"orderId\":\"ORD-123\",\"amount\":1500}'"

# Payment failure (triggers alert + publishes to alerts topic)
./gradlew sendEvent --args="payments.events payment.failed '{\"paymentId\":\"PAY-456\",\"reason\":\"Insufficient funds\"}'"

# Low inventory (triggers alert)
./gradlew sendEvent --args="inventory.events inventory.low '{\"productId\":\"PROD-789\",\"productName\":\"Widget\",\"currentStock\":5}'"
```

### Built-in Routing Rules

| Rule | Trigger | Reactions |
|------|---------|-----------|
| HighValueOrderRule | `order.created` with amount > 1000 | Push to frontend, Log event |
| PaymentFailedRule | `payment.failed` | Push to frontend, Publish to `alerts` topic, Log event |
| InventoryLowRule | `inventory.low` | Push to frontend, Publish to `alerts` topic, Log event |

### Adding Custom Rules

Create a new class implementing `RoutingRule`:

```java
@Component
public class MyCustomRule implements RoutingRule {
    @Override
    public boolean matches(IncomingEvent event) {
        return "my.event.type".equals(event.type());
    }

    @Override
    public List<Reaction> getReactions(IncomingEvent event) {
        return List.of(
            new Reaction.PushToFrontend(notification),
            new Reaction.PublishToKafka("some-topic", payload)
        );
    }
}
```

## Useful Commands

```bash
# View Kafka logs
docker compose logs -f kafka

# Stop all containers
docker compose down

# Stop and remove volumes (clean slate)
docker compose down -v

# Build backend without running
cd backend && ./gradlew build

# Run frontend in production mode
cd frontend && npm run build && npm run preview
```

## ELK Stack (Elasticsearch, Logstash, Kibana)

The project includes ELK for centralized logging and analytics.

### Services

| Service | URL | Purpose |
|---------|-----|---------|
| Elasticsearch | http://localhost:9200 | Stores and indexes events |
| Kibana | http://localhost:5601 | Visualize and search events |
| Logstash | (internal) | Reads from Kafka, writes to Elasticsearch |

### How It Works

```
Kafka Topics ──▶ Logstash ──▶ Elasticsearch ──▶ Kibana
(alerts, *.events)  (transform)   (store/index)    (visualize)
```

Logstash consumes from these Kafka topics:
- `alerts` - Alerts published by routing rules
- `orders.events` - Order events
- `payments.events` - Payment events
- `inventory.events` - Inventory events

### Using Kibana

1. Open http://localhost:5601
2. Go to **Management → Stack Management → Data Views**
3. Create a data view with pattern `events-*`
4. Go to **Analytics → Discover** to search events
5. Go to **Analytics → Dashboard** to build visualizations

### Example Queries in Kibana

```
# Find all payment failures
event_category: "payment.failed"

# Find high-value orders
event_domain: "order" AND amount > 1000

# Find all alerts from last hour
@timestamp >= now-1h AND _index: "events-*"
```

## Kafka UI

Visit http://localhost:8080 to:
- View topics and messages
- Monitor consumer groups
- Inspect broker configuration

## Troubleshooting

**Backend won't start / Kafka connection refused**
- Make sure Kafka is running: `docker compose ps`
- Wait a few seconds after starting Kafka before starting the backend

**Frontend shows "Disconnected"**
- Make sure the backend is running on port 8081
- Check browser console for CORS errors

**No notifications appearing**
- Check Kafka UI at http://localhost:8080 to see if messages are being published
- Check backend logs for consumer errors
