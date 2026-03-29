FROM eclipse-temurin:11-jre-jammy
WORKDIR /app
COPY target/discovery-service-1.0.0.jar discovery-service.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "discovery-service.jar"]
