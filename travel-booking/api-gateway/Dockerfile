FROM eclipse-temurin:11-jre-jammy
WORKDIR /app
COPY target/api-gateway-1.0.0.jar api-gateway.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xms48m", "-Xmx160m", "-XX:MaxMetaspaceSize=80m", "-jar", "api-gateway.jar"]
