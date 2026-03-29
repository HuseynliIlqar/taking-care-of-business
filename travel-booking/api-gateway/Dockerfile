FROM eclipse-temurin:11-jre-jammy
WORKDIR /app
COPY target/api-gateway-1.0.0.jar api-gateway.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]
