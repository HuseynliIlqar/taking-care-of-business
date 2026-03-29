FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/car-rental-service-1.0.0.jar car-rental-service.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "car-rental-service.jar"]
