# Welcome to Taking Care Of Business
***


## Task
Implement a CI/CD pipeline for a microservice-based travel booking application built with Spring Boot and Spring Cloud. The challenge is to automate the full software delivery lifecycle — from building and testing to containerisation and cloud deployment — across 5 independent microservices simultaneously.

The pipeline must:
- Build Docker images for each of the 5 microservices
- Run unit tests (JUnit) for every microservice automatically
- Generate code coverage reports using JaCoCo (minimum 70% line coverage per module)
- Upload coverage to CodeCov
- Deploy the full application to a cloud server (DigitalOcean) on every push to `main`
- Support Jenkins (Jenkinsfile) and Travis CI (.travis.yml) as CI/CD tools
- Orchestrate all services with Docker Compose in both local and production environments

## Description
The Travel Booking System is a Spring Cloud microservices application consisting of 5 services:

| Service | Port | Role |
|---------|------|------|
| `discovery-service` | 8761 | Netflix Eureka server — service registry |
| `api-gateway` | 8080 | Netflix Zuul proxy — single entry point for all clients |
| `flight-service` | 8081 | Flight search with Booking.com API + mock data fallback |
| `hotel-service` | 8082 | Hotel search with Booking.com API + mock data fallback |
| `car-rental-service` | 8083 | Car rental search with Booking.com API + mock data fallback |

Services register themselves with Eureka on startup. The API Gateway discovers them dynamically by name — no hardcoded IPs.

Three CI/CD pipelines are configured in parallel:

| Tool | Config | Deployment |
|------|--------|-----------|
| GitHub Actions | `.github/workflows/ci-cd.yml` | DigitalOcean via SSH |
| Jenkins | `travel-booking/Jenkinsfile` | Docker Hub + Heroku |
| Travis CI | `.travis.yml` | Heroku Container Registry |

Each domain service (flight, hotel, car-rental) integrates with the **Booking.com API via RapidAPI**. Without an API key the services automatically fall back to built-in mock data — all endpoints respond correctly in both modes.

**Live Demo (DigitalOcean):**

| Service | URL |
|---------|-----|
| Eureka Dashboard | http://64.226.68.244:8761 |
| API Gateway | http://64.226.68.244:8080 |
| Flight Service | http://64.226.68.244:8081/flights |
| Hotel Service | http://64.226.68.244:8082/hotels |
| Car Rental | http://64.226.68.244:8083/cars |
| Swagger — Flight | http://64.226.68.244:8081/swagger-ui/index.html |
| Swagger — Hotel | http://64.226.68.244:8082/swagger-ui/index.html |
| Swagger — Car Rental | http://64.226.68.244:8083/swagger-ui/index.html |

## Installation
**Requirements:** Java 11+, Maven 3.6+, Docker, Docker Compose

**Option 1 — Docker Compose (recommended)**
```bash
cd travel-booking
mvn clean package -DskipTests
docker-compose up --build
```
Stop with `docker-compose down`.

**Option 2 — Maven locally**
```bash
# Terminal 1 — start first
cd travel-booking/discovery-service && mvn spring-boot:run

# Terminal 2, 3, 4 — after Eureka is up at http://localhost:8761
cd travel-booking/flight-service && mvn spring-boot:run
cd travel-booking/hotel-service && mvn spring-boot:run
cd travel-booking/car-rental-service && mvn spring-boot:run

# Terminal 5 — start last
cd travel-booking/api-gateway && mvn spring-boot:run
```

**Run tests and coverage:**
```bash
cd travel-booking
mvn clean test jacoco:report
```
Coverage reports: `<service>/target/site/jacoco/index.html`

**Jenkins (local reviewer setup):**
```bash
cd travel-booking
docker-compose -f docker-compose.jenkins.yml up -d
# Open http://localhost:8080
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```
Pipeline setup: New Item → Pipeline → SCM: Git → URL: `https://github.com/HuseynliIlqar/taking-care-of-business` → Script Path: `travel-booking/Jenkinsfile` → Build Now

## Usage
```bash
# Get all flights
curl http://64.226.68.244:8080/flights

# Search flights by route
curl "http://64.226.68.244:8080/flights/search?origin=NYC&destination=LAX"

# Get all hotels
curl http://64.226.68.244:8080/hotels

# Search hotels by city
curl "http://64.226.68.244:8080/hotels/search?location=LosAngeles"

# Get all cars
curl http://64.226.68.244:8080/cars

# Search cars by location
curl "http://64.226.68.244:8080/cars/search?location=Prague"

# Search cars by type
curl "http://64.226.68.244:8080/cars/search?type=SUV"
```

**Available sample data:**
- Flight routes: `NYC→LAX`, `LAX→NYC`, `SFO→ORD`, `BOS→MIA`, `MIA→BOS`, `ORD→SFO`, `NYC→CHI`
- Hotel cities: `LosAngeles`, `NewYork`, `Miami`, `Chicago`, `SanFrancisco`, `Boston`
- Car locations (US mock): `LosAngeles`, `NewYork`, `Miami`, `Chicago`, `SanFrancisco`, `Boston`
- Car locations (EU live API): `Prague`, `London`, `Paris`, `Berlin`, `Rome`, `Amsterdam`
- Car types: `Economy`, `Compact`, `Sedan`, `SUV`, `Luxury`, `Electric`

> **Note:** The Eureka dashboard shows internal Docker hostnames — this is expected Docker behaviour. Use the public IP URLs above to access the services.

### The Core Team


<span><i>Made at <a href='https://qwasar.io'>Qwasar SV -- Software Engineering School</a></i></span>
<span><img alt='Qwasar SV -- Software Engineering School's Logo' src='https://storage.googleapis.com/qwasar-public/qwasar-logo_50x50.png' width='20px' /></span>
