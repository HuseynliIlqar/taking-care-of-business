FROM eclipse-temurin:11-jre-jammy
WORKDIR /app
COPY target/car-rental-service-1.0.0.jar car-rental-service.jar
EXPOSE 8083
ENTRYPOINT ["java", "-Xms64m", "-Xmx180m", "-jar", "car-rental-service.jar"]
