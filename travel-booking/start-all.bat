@echo off
title Travel Booking System - Launcher
color 0A

echo ============================================
echo   TRAVEL BOOKING SYSTEM - STARTING UP
echo ============================================
echo.

echo [1/5] Starting Discovery Service (Eureka)...
start "Discovery Service :8761" cmd /k "cd /d %~dp0discovery-service && mvn spring-boot:run"

echo Waiting 25 seconds for Eureka to be ready...
timeout /t 25 /nobreak > nul
echo Eureka is ready!
echo.

echo [2/5] Starting Flight Service...
start "Flight Service :8081" cmd /k "cd /d %~dp0flight-service && mvn spring-boot:run"

echo [3/5] Starting Hotel Service...
start "Hotel Service :8082" cmd /k "cd /d %~dp0hotel-service && mvn spring-boot:run"

echo [4/5] Starting Car Rental Service...
start "Car Rental Service :8083" cmd /k "cd /d %~dp0car-rental-service && mvn spring-boot:run"

echo Waiting 20 seconds for services to register with Eureka...
timeout /t 20 /nobreak > nul
echo Services registered!
echo.

echo [5/5] Starting API Gateway (Zuul)...
start "API Gateway :8080" cmd /k "cd /d %~dp0api-gateway && mvn spring-boot:run"

echo.
echo ============================================
echo   ALL SERVICES STARTED!
echo ============================================
echo.
echo   Eureka Dashboard : http://localhost:8761
echo   API Gateway      : http://localhost:8080
echo.
echo   TEST ENDPOINTS:
echo   Flights  : http://localhost:8080/flights/search?origin=NYC^&destination=LAX
echo   Hotels   : http://localhost:8080/hotels/search?location=LosAngeles
echo   Cars     : http://localhost:8080/cars/search?location=LosAngeles
echo.
echo   Wait ~30 seconds before testing gateway routes.
echo ============================================
pause
