# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .

# Download dependencies
RUN mvn -B dependency:go-offline

# Copy source code
COPY src ./src

# Build the Spring Boot application
RUN mvn -B clean package -DskipTests


# Stage 2: Run the application
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the generated JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Application port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]