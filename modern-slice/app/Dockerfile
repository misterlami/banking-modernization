# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Cache dependencies before copying sources to speed up rebuilds.
RUN ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B clean package

FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

RUN groupadd --system appgroup && useradd --system --gid appgroup --no-create-home appuser

COPY --from=build /workspace/target/openapi-spring-1.0.0.jar /app/app.jar

EXPOSE 8080
USER appuser

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
