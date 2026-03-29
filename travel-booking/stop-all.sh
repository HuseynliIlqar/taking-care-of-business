#!/bin/bash

echo "============================================"
echo "  TRAVEL BOOKING SYSTEM - SHUTTING DOWN"
echo "============================================"
echo "Stopping all Java processes..."

PIDS=$(pgrep -f "spring-boot:run" 2>/dev/null)

if [ -z "$PIDS" ]; then
    echo "No running Spring Boot processes found."
else
    echo "$PIDS" | xargs kill -15 2>/dev/null
    sleep 3
    # Force kill if still running
    REMAINING=$(pgrep -f "spring-boot:run" 2>/dev/null)
    if [ -n "$REMAINING" ]; then
        echo "$REMAINING" | xargs kill -9 2>/dev/null
    fi
    echo "All services stopped successfully."
fi

echo
echo "============================================"
