# Welcome to Taking Care Of Business
***


## Task
Implement a CI/CD pipeline for a microservice-based travel booking application built with Spring Boot and Spring Cloud. The challenge is to automate the full software delivery lifecycle — from building and testing to containerisation and cloud deployment — across 5 independent microservices simultaneously.

The pipeline must:
- Build Docker images for each of the 5 microservices
- Run unit tests (JUnit) for every microservice automatically on every commit
- Generate code coverage reports using JaCoCo with a minimum of 70% line coverage per module
- Upload coverage results to CodeCov
- Deploy the full application to a cloud server (DigitalOcean) on every push to main
- Support Jenkins (Jenkinsfile) and Travis CI (.travis.yml) as CI/CD tools
- Orchestrate all services with Docker Compose in both local and production environments

## Description
The Travel Booking System is a Spring Cloud microservices application consisting of 5 services that work together to provide flight, hotel, and car rental search functionality.

| Service | Port | Role |
|---------|------|------|
| discovery-service | 8761 | Netflix Eureka server — service registry |
| api-gateway | 8080 | Netflix Zuul proxy — single entry point for all clients |
| flight-service | 8081 | Flight search with Booking.com API and mock data fallback |
| hotel-service | 8082 | Hotel search with Booking.com API and mock data fallback |
| car-rental-service | 8083 | Car rental search with Booking.com API and mock data fallback |

Services register themselves with Eureka on startup. The API Gateway discovers them dynamically by name — no hardcoded IPs required. Each domain service integrates with the Booking.com API via RapidAPI. Without an API key the services automatically fall back to built-in mock data so all endpoints respond correctly in both modes.

Three CI/CD pipelines are configured in parallel. GitHub Actions deploys to DigitalOcean via SSH on every push to main. Jenkins builds Docker images and pushes to Docker Hub then deploys to Heroku. Travis CI builds and deploys to Heroku Container Registry. All three pipelines run tests and generate JaCoCo coverage reports.

Live demo is deployed on DigitalOcean and accessible at the following URLs without any credentials:
- Eureka Dashboard: http://64.226.68.244:8761
- API Gateway: http://64.226.68.244:8080
- Flight Service: http://64.226.68.244:8081/flights
- Hotel Service: http://64.226.68.244:8082/hotels
- Car Rental Service: http://64.226.68.244:8083/cars
- Swagger UI Flight: http://64.226.68.244:8081/swagger-ui/index.html
- Swagger UI Hotel: http://64.226.68.244:8082/swagger-ui/index.html
- Swagger UI Car Rental: http://64.226.68.244:8083/swagger-ui/index.html

## Installation
Requirements: Java 11 or higher, Maven 3.6 or higher, Docker and Docker Compose.

Clone the repository and build all services:
```bash
git clone https://github.com/HuseynliIlqar/taking-care-of-business.git
cd taking-care-of-business/travel-booking
mvn clean package -DskipTests
```

Start all 5 services with Docker Compose:
```bash
docker-compose up --build
```

All services start automatically in the correct order. Discovery service starts first, then domain services, then the API Gateway. Stop everything with:
```bash
docker-compose down
```

To run services locally without Docker, start them in this exact order:
```bash
# Terminal 1 - start first and wait for http://localhost:8761
cd travel-booking/discovery-service && mvn spring-boot:run

# Terminal 2, 3, 4 - after Eureka is up
cd travel-booking/flight-service && mvn spring-boot:run
cd travel-booking/hotel-service && mvn spring-boot:run
cd travel-booking/car-rental-service && mvn spring-boot:run

# Terminal 5 - start last
cd travel-booking/api-gateway && mvn spring-boot:run
```

Run all tests and generate coverage report:
```bash
cd travel-booking
mvn clean test jacoco:report
```

To run Jenkins locally for pipeline review:
```bash
cd travel-booking
docker-compose -f docker-compose.jenkins.yml up -d
```
Open http://localhost:8080 and get the admin password:
```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```
Then create a Pipeline job with SCM Git URL https://github.com/HuseynliIlqar/taking-care-of-business and Script Path travel-booking/Jenkinsfile.

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

Available flight routes: NYC to LAX, LAX to NYC, SFO to ORD, BOS to MIA, MIA to BOS, ORD to SFO, NYC to CHI.

Available hotel cities: LosAngeles, NewYork, Miami, Chicago, SanFrancisco, Boston.

Available car locations in the US with mock data: LosAngeles, NewYork, Miami, Chicago, SanFrancisco, Boston.

Available car locations in Europe with live Booking.com API: Prague, London, Paris, Berlin, Rome, Amsterdam.

Car types: Economy, Compact, Sedan, SUV, Luxury, Electric.

### The Core Team


<span><i>Made at <a href='https://qwasar.io'>Qwasar SV -- Software Engineering School</a></i></span>
<span><img alt='Qwasar SV -- Software Engineering School's Logo' src='https://storage.googleapis.com/qwasar-public/qwasar-logo_50x50.png' width='20px' /></span>
