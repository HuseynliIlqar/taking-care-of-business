FROM eclipse-temurin:11-jre-jammy
WORKDIR /app
COPY target/flight-service-1.0.0.jar flight-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-Xms32m", "-Xmx120m", "-XX:MaxMetaspaceSize=80m", "-jar", "flight-service.jar"]
