# Travel Booking System — Spring Cloud Microservices

A microservice-based travel booking application built with Spring Boot, Netflix Eureka for service discovery, and Netflix Zuul as the API gateway. The project implements a full CI/CD pipeline using GitHub Actions, Jenkins, and Travis CI, with automated testing, code coverage reporting via JaCoCo and CodeCov, and deployment to DigitalOcean.

> This project was developed as part of the [Qwasar](https://qwasar.io) curriculum.

---

## Task

Implement a CI/CD pipeline for a microservice-based travel booking application. The pipeline must:

- Build Docker images for each microservice (5 services total)
- Run unit tests for every microservice using JUnit
- Generate code coverage reports using JaCoCo (minimum 70% line coverage)
- Upload coverage results to CodeCov
- Deploy the application automatically to a cloud platform (DigitalOcean) on every push to `main`
- Support both Jenkins (via `Jenkinsfile`) and Travis CI (via `.travis.yml`) as CI/CD tools
- Use Docker Compose to orchestrate all services locally and in production

---

## Description

The Travel Booking System is composed of 5 independent Spring Boot microservices:

| Service | Port | Responsibility |
|---------|------|---------------|
| `discovery-service` | 8761 | Netflix Eureka server — service registry |
| `api-gateway` | 8080 | Netflix Zuul proxy — single entry point for all clients |
| `flight-service` | 8081 | Flight search (mock data + Booking.com API fallback) |
| `hotel-service` | 8082 | Hotel search (mock data + Booking.com API fallback) |
| `car-rental-service` | 8083 | Car rental search (mock data + Booking.com API fallback) |

### Architecture

```
Client
  │
  ▼
API Gateway (Zuul) :8080
  ├── /flights/**  ──► flight-service      :8081
  ├── /hotels/**   ──► hotel-service       :8082
  └── /cars/**     ──► car-rental-service  :8083
              │
              ▼
    Discovery Service (Eureka) :8761
```

Services register themselves with Eureka on startup. The API Gateway discovers them dynamically by name — no hardcoded IPs required.

### External API Integration

All three domain services integrate with the **Booking.com API via RapidAPI**. If no API key is configured, they automatically fall back to built-in mock data. The application works fully in both modes.

| Service | Booking.com Endpoint |
|---------|---------------------|
| FlightService | `GET /v1/flights/search` |
| HotelService | `GET /v1/hotels/locations` + `GET /v1/hotels/search` |
| CarRentalService | `GET /v1/car-rental/search` |

### CI/CD Pipeline

Three parallel CI/CD configurations are provided:

| Tool | Config file | Deployment target |
|------|------------|-------------------|
| GitHub Actions | `.github/workflows/ci-cd.yml` | DigitalOcean (SSH) |
| Jenkins | `travel-booking/Jenkinsfile` | Docker Hub + Heroku |
| Travis CI | `.travis.yml` | Heroku Container Registry |

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 2.3.12.RELEASE | Application framework |
| Spring Cloud | Hoxton.SR12 | Microservices toolkit |
| Netflix Eureka | — | Service discovery |
| Netflix Zuul | — | API gateway / reverse proxy |
| Docker | — | Containerisation |
| Docker Compose | — | Multi-container orchestration |
| JaCoCo | 0.8.8 | Code coverage (70% minimum) |
| CodeCov | — | Coverage reporting |
| Maven | 3.6+ | Build tool |
| Java | 11 | Runtime |

---

## Installation

### Prerequisites

- Java 11+
- Maven 3.6+
- Docker and Docker Compose

### Option 1 — Docker Compose (recommended)

Build JAR files and start all 5 services with a single command:

```bash
cd travel-booking
mvn clean package -DskipTests
docker-compose up --build
```

All services will start automatically in the correct order (discovery-service first, then domain services, then api-gateway). Stop with:

```bash
docker-compose down
```

### Option 2 — Run locally with Maven

Start services in this exact order:

**Step 1 — Discovery Service**
```bash
cd travel-booking/discovery-service
mvn spring-boot:run
```
Wait until Eureka dashboard is available at http://localhost:8761

**Step 2 — Domain Services** (start in any order, each in a separate terminal)
```bash
cd travel-booking/flight-service && mvn spring-boot:run
cd travel-booking/hotel-service && mvn spring-boot:run
cd travel-booking/car-rental-service && mvn spring-boot:run
```

**Step 3 — API Gateway** (start last)
```bash
cd travel-booking/api-gateway
mvn spring-boot:run
```

Wait ~30 seconds for all services to register with Eureka before testing.

### Optional — Enable Live Booking.com API

Without a key, all services return built-in mock data. To enable real API calls, create `travel-booking/.env`:

```
RAPIDAPI_KEY=your_key_here
RAPIDAPI_HOST=booking-com.p.rapidapi.com
```

Get a free key at [rapidapi.com](https://rapidapi.com) → search "Booking com" → subscribe to the Basic plan.

### Run Tests

```bash
cd travel-booking
mvn clean test
```

### Generate Code Coverage Report

```bash
cd travel-booking
mvn clean test jacoco:report
```

Reports are generated at `<service>/target/site/jacoco/index.html`. The build enforces a **70% line coverage minimum** per module.

### Docker Base Image Note

All Dockerfiles use `eclipse-temurin:11-jre-jammy` instead of `openjdk:11-jre-slim`.
`openjdk:11-jre-slim` was removed from Docker Hub; `eclipse-temurin` is its official
successor maintained by the Adoptium project.

---

## Usage

### Live Demo (DigitalOcean)

All services are deployed and publicly accessible — no credentials required.

| Service | URL |
|---------|-----|
| Eureka Dashboard | http://64.226.68.244:8761 |
| API Gateway | http://64.226.68.244:8080 |
| Flight Service | http://64.226.68.244:8081/flights |
| Hotel Service | http://64.226.68.244:8082/hotels |
| Car Rental Service | http://64.226.68.244:8083/cars |

> **Note:** The Eureka dashboard shows internal Docker hostnames (e.g. `d43ce9e8d88c:api-gateway:8080`) — this is expected Docker behaviour. Use the public IP URLs above to access the services.

### Swagger UI (Interactive API Docs)

Each domain service has its own Swagger UI for interactive endpoint testing:

| Service | Swagger URL |
|---------|------------|
| Flight Service | http://64.226.68.244:8081/swagger-ui/index.html |
| Hotel Service | http://64.226.68.244:8082/swagger-ui/index.html |
| Car Rental Service | http://64.226.68.244:8083/swagger-ui/index.html |

### API Endpoints

#### Via API Gateway (recommended)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/flights` | All available flights |
| GET | `/flights/search?origin=NYC&destination=LAX` | Search flights by route |
| GET | `/hotels` | All available hotels |
| GET | `/hotels/search?location=LosAngeles` | Search hotels by city |
| GET | `/cars` | All available cars |
| GET | `/cars/search?location=LosAngeles` | Search cars by location |
| GET | `/cars/search?type=SUV` | Search cars by type |

#### Example curl Commands

```bash
# Search flights NYC → LAX
curl "http://64.226.68.244:8080/flights/search?origin=NYC&destination=LAX"

# Search hotels in Los Angeles
curl "http://64.226.68.244:8080/hotels/search?location=LosAngeles"

# Search cars in Los Angeles
curl "http://64.226.68.244:8080/cars/search?location=LosAngeles"

# Search SUVs
curl "http://64.226.68.244:8080/cars/search?type=SUV"

# Get all flights
curl http://64.226.68.244:8080/flights
```

### Available Sample Data

**Flight routes:** `NYC→LAX`, `LAX→NYC`, `SFO→ORD`, `BOS→MIA`, `MIA→BOS`, `ORD→SFO`, `NYC→CHI`

**Hotel cities:** `LosAngeles`, `NewYork`, `Miami`, `Chicago`, `SanFrancisco`, `Boston`

**Car locations (US mock):** `LosAngeles`, `NewYork`, `Miami`, `Chicago`, `SanFrancisco`, `Boston`

**Car locations (EU live API):** `Prague`, `London`, `Paris`, `Berlin`, `Rome`, `Amsterdam`

**Car types:** `Economy`, `Compact`, `Sedan`, `SUV`, `Luxury`, `Electric`

### Jenkins (Local — for reviewers)

Run Jenkins locally with Docker:

```bash
cd travel-booking
docker-compose -f docker-compose.jenkins.yml up -d
```

Open **http://localhost:8080** and retrieve the admin password:

```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

**Pipeline setup:**
1. Install suggested plugins
2. New Item → Pipeline → name: `travel-booking-pipeline`
3. Pipeline → Definition: Pipeline script from SCM
4. SCM: Git → URL: `https://github.com/HuseynliIlqar/taking-care-of-business`
5. Script Path: `travel-booking/Jenkinsfile`
6. Save → Build Now

**Jenkins pipeline stages:**

| Stage | Description |
|-------|-------------|
| Checkout | Clone repository |
| Build | `mvn clean package -DskipTests` |
| Run Tests | `mvn test` — JUnit reports published |
| Code Coverage | JaCoCo report (70% line coverage minimum) |
| Docker Build | Builds images for all 5 services |
| Docker Push | Pushes to Docker Hub (`main` branch only) |
| Deploy to Heroku | Container deploy to Heroku (`main` branch only) |

**Required Jenkins credentials** (Manage Jenkins → Credentials):

| ID | Type | Description |
|----|------|-------------|
| `dockerhub-credentials` | Username/Password | Docker Hub account |
| `heroku-api-key` | Secret text | Heroku API key |

### GitHub Actions Secrets

| Secret | Description |
|--------|-------------|
| `DO_HOST` | DigitalOcean server IP |
| `DO_USER` | SSH username (root) |
| `DO_PASSWORD` | SSH password |
| `CODECOV_TOKEN` | CodeCov project token (optional) |
