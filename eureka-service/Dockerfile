# Stage 1: Build from repo root
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY . .

# Fix 1: Build ONLY eureka-service and skip test compilation
RUN mvn clean package -Dmaven.test.skip=true -pl eureka-service -am

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Fix 2: Copy from the specific eureka-service target folder
COPY --from=build /app/eureka-service/target/*.jar app.jar

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]