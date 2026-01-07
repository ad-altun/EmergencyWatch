# EmergencyWatch

A microservices-based **emergency vehicle fleet observability platform** built with Java 21, Spring Boot 3, React 19, Apache Kafka, PostgreSQL, and MongoDB.


## Overview

EmergencyWatch monitors fire trucks, ambulances, and police vehicles in real-time, tracking telemetry data like fuel levels, engine temperature, battery voltage, emergency status, and location. The system detects critical conditions and generates alerts for fleet operators through a modern web dashboard.

This project demonstrates:
- **Microservices architecture** with event-driven communication
- **Modern React frontend** with real-time data visualization
- **Polyglot persistence** (PostgreSQL for operational data, MongoDB for analytics)
- **Real-time data processing** with Apache Kafka
- **Cloud-native deployment** on Azure with Container Apps and API Management
- **Professional testing practices** with JUnit 5 and Mockito
- **CI/CD integration** with SonarQube Cloud quality gates

## Tech Stack

### Backend
| Category | Technologies |
|----------|--------------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.5.7 |
| **Messaging** | Apache Kafka (KRaft mode) |
| **Databases** | PostgreSQL 17, MongoDB 7.0 |
| **Build** | Maven |
| **Testing** | JUnit 5, Mockito, AssertJ |
| **API Documentation** | OpenAPI 3.0 / Swagger UI |

### Frontend
| Category | Technologies |
|----------|--------------|
| **Language** | TypeScript 5.9 |
| **Framework** | React 19 |
| **Build Tool** | Vite 7 |
| **Styling** | Tailwind CSS 3.4 |
| **State Management** | React Query (TanStack Query) |
| **HTTP Client** | Axios |
| **Charts** | Chart.js + react-chartjs-2 |
| **UI Components** | Radix UI |

### Infrastructure
| Category | Technologies |
|----------|--------------|
| **Containerization** | Docker, Docker Compose |
| **CI/CD** | Jenkins |
| **Code Quality** | SonarQube Cloud |

### Azure Cloud (Production)
| Category | Technologies |
|----------|--------------|
| **Compute** | Azure Container Apps |
| **API Gateway** | Azure API Management |
| **Frontend Hosting** | Azure Static Web Apps |
| **Container Registry** | Azure Container Registry |
| **Message Broker** | Azure Event Hubs (Kafka API) |
| **Relational Database** | Azure Database for PostgreSQL |
| **Document Database** | Azure Cosmos DB (MongoDB API) |

## Architecture

### Backend Microservices

#### 1. Vehicle Simulator
Generates realistic telemetry data for a fleet of emergency vehicles.

- Simulates fire trucks (24V systems), ambulances (12V), and police vehicles (12V)
- Produces telemetry every 3 seconds per vehicle
- Publishes to `vehicle-telemetry` Kafka topic

#### 2. Data Processor
Consumes telemetry, validates data, detects alert conditions, and persists to PostgreSQL.

**Alert Detection:**:

| Alert Type | Condition |
|------------|-----------|
| LOW_FUEL | Fuel level < 20% |
| HIGH_ENGINE_TEMP | Engine temperature > 95°C |
| LOW_BATTERY | < 11V (12V systems) or < 22V (24V systems) |
| EMERGENCY_STATUS_CHANGE | Emergency lights activated |

#### 3. Analytics Service
Provides real-time fleet analytics and historical metrics aggregation.

**Features:**
- Real-time fleet metrics (average speed, fuel consumption, vehicle counts)
- Per-vehicle analytics with status and type distribution
- Scheduled daily aggregation job (PostgreSQL → MongoDB)
- REST API for querying metrics

#### 4. Notification Service
Manages vehicle alerts with deduplication and lifecycle tracking.

**Features:**
- Alert deduplication (prevents duplicate active alerts)
- Status management (ACTIVE → ACKNOWLEDGED → RESOLVED)
- Filtering by vehicle type and status
- REST API for alert management

### Frontend Dashboard
A modern React-based web application for monitoring the emergency vehicle fleet.

**Features:**
- Real-time fleet dashboard with vehicle status overview
- Interactive alerts panel with acknowledge/resolve actions
- Fleet analytics with charts (fuel consumption, speed trends)
- Historical metrics visualization
- Dark mode support
- Responsive design

**Pages:**
- **Dashboard**: Live vehicle status, active alerts, fleet averages
- **Analytics**: Historical metrics, vehicle fuel consumption, speed trends by type

## Getting Started

### Prerequisites
- Java 21
- Maven 3.8+
- Node.js 18+ & npm
- Docker & Docker Compose

### 1. Start Infrastructure

```bash
docker-compose up -d
```

This starts:
- Kafka (port 9092)
- PostgreSQL (port 55432)
- MongoDB (port 27017)

### 2. Run the Services

Open separate terminals for each service:

```bash
# Terminal 1: Vehicle Simulator
cd services/vehicle-simulator
mvn spring-boot:run

# Terminal 2: Data Processor
cd services/data-processor
mvn spring-boot:run

# Terminal 3: Analytics Service
cd services/analytics-service
mvn spring-boot:run

# Terminal 4: Notification Service
cd services/notification-service
mvn spring-boot:run

# Terminal 5: Frontend
cd frontend
npm install
npm run dev
```

### 3. Access the Application

- **Frontend Dashboard**: http://localhost:5173
- **Analytics API**: http://localhost:8082
- **Notifications API**: http://localhost:8083

### 4. Verify the System

```bash
# Check fleet analytics
curl http://localhost:8082/api/analytics/fleet

# Check active alerts
curl http://localhost:8083/api/alerts/active

# Check analytics stats
curl http://localhost:8082/api/analytics/stats
```

## API Documentation

Full interactive API documentation is available via **Swagger UI** when running locally:

| Service              | Swagger UI                            | OpenAPI Spec                                                                          |
|----------------------|---------------------------------------|---------------------------------------------------------------------------------------|
| Analytics Service    | http://localhost:8082/swagger-ui.html | [openapi-analytics.json](docs/api-documentation/swagger/openapi-analytics.yaml)       |
| Notification Service | http://localhost:8083/swagger-ui.html | [openapi-notification.json](docs/api-documentation/swagger/openapi-notification.yaml) |

**View API Documentation Online:** 

[ad-altun.github.io/EmergencyWatch/api-documentation/swagger/](https://ad-altun.github.io/EmergencyWatch/api-documentation/swagger/)

Or run locally for interactive docs:
- Analytics: http://localhost:8082/swagger-ui.html
- Notifications: http://localhost:8083/swagger-ui.html


### Quick Reference

| Service       | Method | Endpoint                       | Description                  |
|---------------|--------|--------------------------------|------------------------------|
| Analytics     | GET    | `/api/analytics/fleet`         | Fleet-wide metrics           |
| Analytics     | GET    | `/api/analytics/vehicles`      | All vehicle analytics        |
| Analytics     | GET    | `/api/analytics/history`       | Historical metrics (MongoDB) |
| Notifications | GET    | `/api/alerts/active`           | Active alerts                |
| Notifications | PATCH  | `/api/alerts/{id}/acknowledge` | Acknowledge alert            |
| Notifications | PATCH  | `/api/alerts/{id}/resolve`     | Resolve alert                |

## Testing

The project includes comprehensive unit tests with ~76 tests across all services.

```bash
# Run all tests
cd services/data-processor && mvn test
cd services/notification-service && mvn test
cd services/analytics-service && mvn test
```

### Test Coverage

| Service              | Test Classes                   | Key Areas Tested                        |
|----------------------|--------------------------------|-----------------------------------------|
| notification-service | AlertServiceTest               | Deduplication, status transitions, CRUD |
| data-processor       | TelemetryProcessingServiceTest | Alert detection, thresholds, validation |
| data-processor       | AlertPublisherTest             | Kafka publishing, error handling        |
| analytics-service    | AnalyticsServiceTest           | Metric calculations, fleet aggregations |
| analytics-service    | MetricsAggregationServiceTest  | Daily aggregation, idempotency          |

## Project Structure

```
EmergencyWatch/
├── docker-compose.yml              # Infrastructure setup
├── .github/workflows/              # CI/CD pipelines
├── docs/
│   └── api-documentation/swagger/  # OpenAPI specs & Swagger UI
├── frontend/                       # React Frontend
│   ├── src/
│   │   ├── api/                    # API client services
│   │   ├── components/
│   │   │   ├── charts/             # Chart.js visualizations
│   │   │   ├── dashboard/          # Dashboard components
│   │   │   ├── layout/             # Header, Sidebar, MainLayout
│   │   │   └── ui/                 # Base UI components
│   │   ├── hooks/                  # React Query hooks
│   │   ├── pages/                  # Dashboard & Analytics pages
│   │   └── types/                  # TypeScript interfaces
│   └── package.json
├── services/
│   ├── vehicle-simulator/          # Telemetry generator
│   │   └── src/main/java/.../
│   │       ├── service/
│   │       └── model/
│   ├── data-processor/             # Telemetry processing & alerts
│   │   └── src/main/java/.../
│   │       ├── service/
│   │       ├── consumer/
│   │       └── model/
│   ├── analytics-service/          # Real-time & historical analytics
│   │   └── src/main/java/.../
│   │       ├── service/
│   │       ├── scheduler/
│   │       ├── controller/
│   │       └── entity/
│   └── notification-service/       # Alert management
│       └── src/main/java/.../
│           ├── service/
│           ├── consumer/
│           └── controller/
```

## Design Decisions

### Why Polyglot Persistence?
- **PostgreSQL** for operational data requiring ACID compliance (telemetry, alerts)
- **MongoDB** for flexible analytics schemas that may evolve over time

### Why Kafka?
- Decouples services for independent scaling and deployment
- Enables event replay for debugging and reprocessing
- Handles high-throughput telemetry data efficiently

### Why Separate Alert Topic?
- Isolates critical alerts from high-volume telemetry
- Allows independent scaling of notification handling
- Enables future integrations (email, SMS, push notifications)

## Configuration

### Service Ports

| Service | Port |
|---------|------|
| Frontend | 5173 |
| Data Processor | 8080 |
| Analytics Service | 8082 |
| Notification Service | 8083 |
| PostgreSQL | 55432 |
| Kafka | 9092 |
| MongoDB | 27017 |

### Vehicle Types & Battery Thresholds

| Vehicle Type | Battery System | Low Battery Threshold |
|--------------|----------------|----------------------|
| FIRE_TRUCK | 24V | < 22V |
| AMBULANCE | 12V | < 11V |
| POLICE | 12V | < 11V |

## Background

This project leverages my 2.5 years of experience developing embedded software 
for fire trucks, where I worked with CAN bus systems and vehicle telemetry. 
EmergencyWatch translates that domain knowledge into a modern microservices architecture, 
demonstrating how real-world emergency vehicle data could be processed and analyzed at scale.

---

## Project Stats

![GitHub repo size](https://img.shields.io/github/repo-size/ad-altun/EmergencyWatch)
![GitHub last commit](https://img.shields.io/github/last-commit/ad-altun/EmergencyWatch)
![GitHub language count](https://img.shields.io/github/languages/count/ad-altun/EmergencyWatch)
![GitHub top language](https://img.shields.io/github/languages/top/ad-altun/EmergencyWatch)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ad-altun_EmergencyWatch&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ad-altun_EmergencyWatch)

--- 

## License

MIT License - see [LICENSE](LICENSE) file for details.


---


## Contact

<a href="https://github.com/ad-altun"><img src="https://img.shields.io/badge/GitHub-ad--altun-181717?style=flat&logo=github" alt="GitHub"></a>&nbsp;&nbsp;&nbsp;
<a href="https://www.linkedin.com/in/abidin-deniz-altun-46906a71/"><img src="https://img.shields.io/badge/LinkedIn-Connect-0A66C2?style=flat&logo=linkedin" alt="LinkedIn"></a>&nbsp;&nbsp;&nbsp;
<a href="https://denizaltun.de"><img src="https://img.shields.io/badge/Portfolio-denizaltun.de-000000?style=flat&logo=google-chrome&logoColor=white" alt="Portfolio"></a>&nbsp;&nbsp;&nbsp;
<a href="mailto:contact@denizaltun.de"><img src="https://img.shields.io/badge/Email-Contact-EA4335?style=flat&logo=gmail" alt="Email"></a>

---

⭐ **Star this repository** if you find it helpful!