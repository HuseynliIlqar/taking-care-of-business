#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "============================================"
echo "  TRAVEL BOOKING SYSTEM - STARTING UP"
echo "============================================"
echo

echo "[1/5] Starting Discovery Service (Eureka)..."
cd "$SCRIPT_DIR/discovery-service"
mvn spring-boot:run > /tmp/discovery-service.log 2>&1 &
DISCOVERY_PID=$!
echo "  PID: $DISCOVERY_PID"

echo "Waiting 25 seconds for Eureka to be ready..."
sleep 25
echo "Eureka is ready!"
echo

echo "[2/5] Starting Flight Service..."
cd "$SCRIPT_DIR/flight-service"
mvn spring-boot:run > /tmp/flight-service.log 2>&1 &
echo "  PID: $!"

echo "[3/5] Starting Hotel Service..."
cd "$SCRIPT_DIR/hotel-service"
mvn spring-boot:run > /tmp/hotel-service.log 2>&1 &
echo "  PID: $!"

echo "[4/5] Starting Car Rental Service..."
cd "$SCRIPT_DIR/car-rental-service"
mvn spring-boot:run > /tmp/car-rental-service.log 2>&1 &
echo "  PID: $!"

echo "Waiting 20 seconds for services to register with Eureka..."
sleep 20
echo "Services registered!"
echo

echo "[5/5] Starting API Gateway (Zuul)..."
cd "$SCRIPT_DIR/api-gateway"
mvn spring-boot:run > /tmp/api-gateway.log 2>&1 &
echo "  PID: $!"

echo
echo "============================================"
echo "  ALL SERVICES STARTED!"
echo "============================================"
echo
echo "  Eureka Dashboard : http://localhost:8761"
echo "  API Gateway      : http://localhost:8080"
echo
echo "  TEST ENDPOINTS:"
echo "  Flights  : http://localhost:8080/flights/search?origin=NYC&destination=LAX"
echo "  Hotels   : http://localhost:8080/hotels/search?location=LosAngeles"
echo "  Cars     : http://localhost:8080/cars/search?location=LosAngeles"
echo
echo "  Logs: /tmp/*.log"
echo "  Wait ~30 seconds before testing gateway routes."
echo "  To stop all: ./stop-all.sh"
echo "============================================"
