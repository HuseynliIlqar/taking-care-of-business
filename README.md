# Travel Booking Microservices — CI/CD Pipeline

A microservice-based travel booking application with a full CI/CD pipeline using Docker, Jenkins, Travis CI, JaCoCo, and Heroku.

---

## Architecture

```
travel-booking/
├── discovery-service/     # Eureka Server — service registry (port 8761)
├── api-gateway/           # Spring Cloud Gateway — single entry point (port 8080)
├── flight-service/        # Flight management & booking (port 8081)
├── hotel-service/         # Hotel management & booking (port 8082)
├── car-rental-service/    # Car rental management (port 8083)
├── docker-compose.yml
├── Jenkinsfile
├── .travis.yml
└── README.md
```

### Service Ports

| Service           | Port |
|-------------------|------|
| Discovery Service | 8761 |
| API Gateway       | 8080 |
| Flight Service    | 8081 |
| Hotel Service     | 8082 |
| Car Rental        | 8083 |

---

## Tech Stack

- **Java 11** + **Spring Boot 2.7.18**
- **Spring Cloud 2021.0.8** (Eureka, Gateway)
- **H2** in-memory database (per service)
- **JUnit 5** + **Mockito** for unit testing
- **JaCoCo** for code coverage
- **Docker** + **Docker Compose**
- **Jenkins** or **Travis CI** for CI/CD
- **Heroku** for cloud deployment

---

## Installation

### Prerequisites

- Java 11+
- Maven 3.8+
- Docker & Docker Compose

### Build All Services

```bash
mvn clean package
```

### Run Locally with Docker Compose

```bash
docker-compose up --build
```

This starts all five services. Visit `http://localhost:8761` to see the Eureka dashboard.

### Run a Single Service Locally

```bash
cd flight-service
mvn spring-boot:run
```

> Note: Set `eureka.client.service-url.defaultZone=http://localhost:8761/eureka/` in `application.yml` for local runs.

---

## API Endpoints (via API Gateway at port 8080)

### Flights — `/api/flights`

| Method | Endpoint                     | Description           |
|--------|------------------------------|-----------------------|
| GET    | `/api/flights`               | List all flights      |
| GET    | `/api/flights/{id}`          | Get flight by ID      |
| GET    | `/api/flights/search?origin=X&destination=Y` | Search flights |
| GET    | `/api/flights/available`     | Available flights     |
| POST   | `/api/flights`               | Create flight         |
| PUT    | `/api/flights/{id}`          | Update flight         |
| DELETE | `/api/flights/{id}`          | Delete flight         |
| POST   | `/api/flights/{id}/book`     | Book a seat           |

### Hotels — `/api/hotels`

| Method | Endpoint                          | Description            |
|--------|-----------------------------------|------------------------|
| GET    | `/api/hotels`                     | List all hotels        |
| GET    | `/api/hotels/{id}`                | Get hotel by ID        |
| GET    | `/api/hotels/search?location=X`   | Hotels by location     |
| GET    | `/api/hotels/available?location=X`| Available in location  |
| GET    | `/api/hotels/stars?min=4`         | Hotels by min stars    |
| POST   | `/api/hotels`                     | Create hotel           |
| PUT    | `/api/hotels/{id}`                | Update hotel           |
| DELETE | `/api/hotels/{id}`                | Delete hotel           |
| POST   | `/api/hotels/{id}/book`           | Book a room            |

### Cars — `/api/cars`

| Method | Endpoint                            | Description             |
|--------|-------------------------------------|-------------------------|
| GET    | `/api/cars`                         | List all cars           |
| GET    | `/api/cars/{id}`                    | Get car by ID           |
| GET    | `/api/cars/available`               | Available cars          |
| GET    | `/api/cars/search?location=X`       | Cars by location        |
| GET    | `/api/cars/category?category=SUV`   | Cars by category        |
| POST   | `/api/cars`                         | Add car                 |
| PUT    | `/api/cars/{id}`                    | Update car              |
| DELETE | `/api/cars/{id}`                    | Delete car              |
| POST   | `/api/cars/{id}/rent`               | Rent a car              |
| POST   | `/api/cars/{id}/return`             | Return a car            |

---

## Testing

### Run All Tests

```bash
mvn test
```

### Generate Coverage Report (JaCoCo)

```bash
mvn jacoco:report
```

Reports are generated at `target/site/jacoco/index.html` in each service directory.

---

## CI/CD Pipeline

### Jenkins

1. Install Jenkins with the Maven, Docker, and JaCoCo plugins.
2. Create a pipeline job pointing to this repository.
3. Set credentials in Jenkins:
   - `DOCKERHUB_CREDENTIALS` — Docker Hub username/password
   - `HEROKU_API_KEY` — Heroku API token
4. The `Jenkinsfile` at the project root defines the pipeline stages:
   - **Checkout** → **Build** → **Test** → **Code Coverage** → **Docker Build** → **Docker Push** → **Deploy to Heroku**

### Travis CI

1. Connect the repository to [Travis CI](https://travis-ci.com).
2. Set environment variables in the Travis CI repository settings:
   - `DOCKER_USERNAME`
   - `DOCKER_PASSWORD`
   - `HEROKU_API_KEY`
3. The `.travis.yml` file handles build, test, coverage upload to Codecov, Docker image publishing, and Heroku deployment automatically on pushes to `main`.

---

## Cloud Deployment (Heroku)

Each microservice is deployed as a separate Heroku app:

| Service           | Heroku App                    |
|-------------------|-------------------------------|
| Discovery Service | `travel-booking-discovery-service` |
| API Gateway       | `travel-booking-api-gateway`  |
| Flight Service    | `travel-booking-flight-service` |
| Hotel Service     | `travel-booking-hotel-service` |
| Car Rental        | `travel-booking-car-rental-service` |

Deploy manually:

```bash
heroku container:login
heroku container:push web --app travel-booking-flight-service
heroku container:release web --app travel-booking-flight-service
```

---

<span><i>Made at <a href='https://qwasar.io'>Qwasar SV -- Software Engineering School</a></i></span>
<span><img alt='Qwasar SV -- Software Engineering School Logo' src='https://storage.googleapis.com/qwasar-public/qwasar-logo_50x50.png' width='20px' /></span>
