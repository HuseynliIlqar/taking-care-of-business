FROM eclipse-temurin:11-jre-jammy
WORKDIR /app
COPY target/api-gateway-1.0.0.jar api-gateway.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xms64m", "-Xmx180m", "-jar", "api-gateway.jar"]
