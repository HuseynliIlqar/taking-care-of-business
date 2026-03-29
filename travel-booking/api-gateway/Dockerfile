FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/api-gateway-1.0.0.jar api-gateway.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]
