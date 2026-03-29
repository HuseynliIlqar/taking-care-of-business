FROM eclipse-temurin:11-jre-jammy
WORKDIR /app
COPY target/flight-service-1.0.0.jar flight-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-Xms64m", "-Xmx180m", "-jar", "flight-service.jar"]
