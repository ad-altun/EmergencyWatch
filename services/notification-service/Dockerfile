# =========================
# Build stage
# =========================
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

RUN apk add --no-cache maven

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup

USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8083

# JVM tuned for containers (ACA friendly)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+UseContainerSupport", "-jar", "app.jar"]
