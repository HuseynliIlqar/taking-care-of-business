FROM eclipse-temurin:11-jre-jammy
WORKDIR /app
COPY target/hotel-service-1.0.0.jar hotel-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-Xms64m", "-Xmx180m", "-jar", "hotel-service.jar"]
