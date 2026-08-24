# Stage 1: Build from repo root
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -pl eureka-service -am

# Stage 2: Run application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/eureka-service/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
