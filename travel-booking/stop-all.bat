@echo off
title Travel Booking System - Shutdown
color 0C

echo ============================================
echo   TRAVEL BOOKING SYSTEM - SHUTTING DOWN
echo ============================================
echo.
echo Stopping all Java processes...

taskkill /F /IM java.exe /T 2>nul

if %errorlevel% == 0 (
    echo All services stopped successfully.
) else (
    echo No running Java processes found.
)

echo.
echo ============================================
pause
