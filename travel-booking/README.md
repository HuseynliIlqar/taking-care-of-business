# Travel Booking System — Spring Cloud Microservices

A microservice-based travel booking application using **Netflix Eureka** for service discovery and **Netflix Zuul** as the API gateway.

> **API istifadə təlimatı, düzgün input dəyərləri və nümunə cavablar üçün:**
> **[API_DOCS.md](./API_DOCS.md)** — bütün endpoint-lər, mövcud şəhər/marşrut siyahısı və sınaq qaydası burada ətraflı izah edilib.

## Live Demo (DigitalOcean)

All services are deployed and publicly accessible — no credentials required.

### Service Endpoints

| Service | URL |
|---------|-----|
| Eureka Dashboard | http://64.226.68.244:8761 |
| API Gateway | http://64.226.68.244:8080 |
| Flight Service | http://64.226.68.244:8081/flights |
| Hotel Service | http://64.226.68.244:8082/hotels |
| Car Rental Service | http://64.226.68.244:8083/cars |

### Swagger UI (Interactive API Docs)

Each domain service has its own Swagger UI. Open in browser to explore and test all endpoints interactively:

| Service | Swagger URL |
|---------|------------|
| Flight Service | http://64.226.68.244:8081/swagger-ui/index.html |
| Hotel Service | http://64.226.68.244:8082/swagger-ui/index.html |
| Car Rental Service | http://64.226.68.244:8083/swagger-ui/index.html |

> **Note:** The Eureka dashboard may show internal Docker hostnames (e.g. `d43ce9e8d88c:api-gateway:8080`) — this is expected Docker behaviour. Use the public IP URLs above to access the services.

## Architecture

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

## Project Structure

```
travel-booking/
├── pom.xml                    (parent POM — Spring Boot 2.3.12, Spring Cloud Hoxton.SR12)
├── discovery-service/         (Eureka Server — port 8761)
├── api-gateway/               (Zuul Proxy — port 8080)
├── flight-service/            (Flight Search — port 8081)
├── hotel-service/             (Hotel Search — port 8082)
└── car-rental-service/        (Car Rental Search — port 8083)
```

## External API Integration

All three services integrate with the **Booking.com API via RapidAPI**:

| Service | Endpoint Used |
|---------|--------------|
| FlightService | `GET /v1/flights/search` |
| HotelService | `GET /v1/hotels/locations` + `GET /v1/hotels/search` |
| CarRentalService | `GET /v1/car-rental/search` |

### ⚠️ API Key Required — Read Before Reviewing

> The external API calls will only execute if a valid `RAPIDAPI_KEY` is present.
> **Without a key, all three services automatically fall back to built-in mock data.**
> The application starts and all endpoints respond correctly in both modes.

To enable live API calls, create the file `travel-booking/.env`:

```
RAPIDAPI_KEY=your_key_here
RAPIDAPI_HOST=booking-com.p.rapidapi.com
```

Get a free key at [rapidapi.com](https://rapidapi.com) → search **"Booking com"** → subscribe to the Basic plan.

### ⚠️ RapidAPI Basic Plan Limits

The project is built on the **RapidAPI Basic (free) plan**, which has hard limits:

| Limit | Value |
|-------|-------|
| **Monthly requests** | **530 requests / month** |
| **Rate limit** | **5 requests / second** |
| **Monthly bandwidth** | **10,240 MB / month** |

**What this means for reviewers:**

- Do **not** run automated test suites that hit the live API in a loop — this will exhaust the monthly quota instantly.
- Test each endpoint **manually**, one request at a time.
- The HotelService makes **2 API calls per search** (location lookup + hotel search) — keep this in mind.
- If the API returns an error (quota exceeded or key invalid), the service **silently falls back to mock data** and still returns a valid JSON response. This is intentional, not a bug.
- To verify that live API integration is working, check the application logs — a successful external call logs at DEBUG level; a fallback logs a WARN message.

## Prerequisites

- Java 11+
- Maven 3.6+

## Build

```bash
cd travel-booking
mvn clean install
```

## Running the Application

**Start services in this exact order:**

### 1. Discovery Service (start first)
```bash
cd discovery-service
mvn spring-boot:run
```
Wait until the Eureka dashboard is available at http://localhost:8761

### 2. Domain Services (start in any order)
```bash
# Terminal 1
cd flight-service && mvn spring-boot:run

# Terminal 2
cd hotel-service && mvn spring-boot:run

# Terminal 3
cd car-rental-service && mvn spring-boot:run
```

### 3. API Gateway (start last)
```bash
cd api-gateway
mvn spring-boot:run
```

Wait ~30 seconds for all services to register with Eureka before testing gateway routes.

## API Endpoints

### Via API Gateway (recommended)

| Method | URL | Description |
|--------|-----|-------------|
| GET | `http://localhost:8080/flights` | All available flights |
| GET | `http://localhost:8080/flights/search?origin=NYC&destination=LAX` | Search flights by route |
| GET | `http://localhost:8080/hotels` | All available hotels |
| GET | `http://localhost:8080/hotels/search?location=LosAngeles` | Search hotels by city |
| GET | `http://localhost:8080/cars` | All available cars |
| GET | `http://localhost:8080/cars/search?location=LosAngeles` | Search cars by location |
| GET | `http://localhost:8080/cars/search?type=SUV` | Search cars by type |

### Direct Service Calls

| Service | Base URL |
|---------|----------|
| Flight Service | `http://localhost:8081/flights` |
| Hotel Service | `http://localhost:8082/hotels` |
| Car Rental Service | `http://localhost:8083/cars` |

## Example curl Commands

```bash
# Search flights NYC → LAX
curl "http://localhost:8080/flights/search?origin=NYC&destination=LAX"

# Search hotels in Los Angeles
curl "http://localhost:8080/hotels/search?location=LosAngeles"

# Search cars in Los Angeles
curl "http://localhost:8080/cars/search?location=LosAngeles"

# Search SUVs in any location
curl "http://localhost:8080/cars/search?type=SUV"

# Get all flights
curl http://localhost:8080/flights

# Eureka dashboard
open http://localhost:8761
```

## Sample Flight Data

Available origins/destinations: `NYC`, `LAX`, `SFO`, `ORD`, `BOS`, `MIA`, `CHI`

## Sample Hotel Cities

Available cities: `LosAngeles`, `NewYork`, `Miami`, `Chicago`, `SanFrancisco`, `Boston`

## Sample Car Locations

**US cities (mock data fallback — Booking.com car rental API does not cover US locations):**
`LosAngeles`, `NewYork`, `Miami`, `Chicago`, `SanFrancisco`, `Boston`

**European cities (live Booking.com API — real results returned when key is configured):**
`Prague`, `London`, `Paris`, `Berlin`, `Rome`, `Amsterdam`

Car types: `Economy`, `Compact`, `Sedan`, `SUV`, `Luxury`, `Electric`

> To test live car rental data, use a European city:
> ```
> GET http://localhost:8080/cars/search?location=Prague
> GET http://localhost:8080/cars/search?location=London
> ```

## Docker Base Image Note

All Dockerfiles use `eclipse-temurin:11-jre-jammy` instead of `openjdk:11-jre-slim`.
`openjdk:11-jre-slim` was removed from Docker Hub; `eclipse-temurin` is its official successor
maintained by the Adoptium project and is the recommended replacement.

## CI/CD Pipeline

### 1. Run Application with Docker Compose

Build JAR files first, then start all 5 services:

```bash
cd travel-booking
mvn clean package -DskipTests
docker-compose up --build
```

Services will be available at the same ports as local mode. Stop with `docker-compose down`.

---

### 2. GitHub Actions (Active CI/CD)

Every push to `main` triggers the full pipeline automatically:

| Job | Steps |
|-----|-------|
| **Build & Test** | `mvn clean package` → `mvn test` → JaCoCo report → CodeCov upload |
| **Docker Build** | Builds Docker image for each of the 5 services |
| **Deploy to Heroku** | Pushes images to Heroku Container Registry → releases all 5 apps |

**Required GitHub Secrets** (Settings → Secrets → Actions):

| Secret | Description |
|--------|-------------|
| `HEROKU_API_KEY` | `heroku auth:token` çıxışı |
| `CODECOV_TOKEN` | codecov.io project token (optional) |

**Live URLs after deploy:**

| Service | URL |
|---------|-----|
| API Gateway | https://api-gateway.herokuapp.com |
| Flight Service | https://flight-service.herokuapp.com/flights |
| Hotel Service | https://hotel-service.herokuapp.com/hotels |
| Car Rental | https://car-rental-service.herokuapp.com/cars |
| Eureka Dashboard | https://discoveryservice.herokuapp.com |

---

### 3. Jenkins (Local — for reviewers)

Run Jenkins locally with a single Docker command:

```bash
cd travel-booking
docker-compose -f docker-compose.jenkins.yml up -d
```

Open **http://localhost:8080** and get the initial admin password:

```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

**Pipeline setup in Jenkins UI:**
1. Install suggested plugins
2. **New Item** → **Pipeline** → name: `travel-booking-pipeline`
3. Pipeline → Definition: **Pipeline script from SCM**
4. SCM: **Git** → URL: `https://github.com/HuseynliIlqar/taking-care-of-business`
5. Script Path: `travel-booking/Jenkinsfile`
6. **Save** → **Build Now**

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

**Jenkins credentials** (Manage Jenkins → Credentials):

| ID | Type | Description |
|----|------|-------------|
| `dockerhub-credentials` | Username/Password | Docker Hub account |
| `heroku-api-key` | Secret text | Heroku API key |

---

### 4. Travis CI (alternative configuration)

`.travis.yml` at the repo root defines the same pipeline for Travis CI.

Set these environment variables in Travis CI project settings:

```
HEROKU_API_KEY=your_heroku_api_key
CODECOV_TOKEN=your_codecov_token
```

---

### Code Coverage — JaCoCo

JaCoCo is configured in the parent `pom.xml`. HTML reports generated at:

```
<service>/target/site/jacoco/index.html
```

Run locally:
```bash
cd travel-booking
mvn clean test jacoco:report
```

The build enforces a **70% line coverage minimum** per module.

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 2.3.12.RELEASE | Application framework |
| Spring Cloud | Hoxton.SR12 | Microservices toolkit |
| Netflix Eureka | — | Service discovery |
| Netflix Zuul | — | API gateway / reverse proxy |
| Spring Web | — | REST API |
| Maven | 3.6+ | Build tool |
| Java | 11 | Runtime |
