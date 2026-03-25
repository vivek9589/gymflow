# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
# Using 'jre' instead of 'jdk' for the final stage makes the image smaller/more secure
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Using a wildcard ensures we catch the JAR even if the version changes
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
# Explicitly setting the prod profile in the entrypoint
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]