# Stage 1: Build from repo root
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY . .

# Skip test execution and test compilation completely for gateway-service
RUN mvn clean package -Dmaven.test.skip=true -DskipTests -pl gateway-service -am

# Stage 2: Run application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the generated gateway jar file
COPY --from=build /app/gateway-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]