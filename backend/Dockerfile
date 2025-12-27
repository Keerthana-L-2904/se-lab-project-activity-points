# ============================================
# STAGE 1: BUILD
# ============================================
# Use Maven image with Java 17 to build the application
FROM maven:3.9-eclipse-temurin-21 AS build

# Set working directory inside container
WORKDIR /app

# Copy only pom.xml first (for dependency caching)
# This layer will be cached if pom.xml doesn't change
COPY pom.xml .

# Download all dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy the entire source code
COPY src ./src

# Build the application (creates a .jar file in target/ directory)
# -DskipTests skips running tests during build
RUN mvn clean package -DskipTests

# ============================================
# STAGE 2: RUN
# ============================================
# Use lightweight Java runtime image (smaller size)
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
# The * wildcard handles different jar names (backend-0.0.1-SNAPSHOT.jar, etc.)
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 (Spring Boot default)
EXPOSE 8080

# Run the application
# Environment variables will be injected at runtime by docker-compose
ENTRYPOINT ["java", "-jar", "app.jar"]