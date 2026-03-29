FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/hotel-service-1.0.0.jar hotel-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "hotel-service.jar"]
