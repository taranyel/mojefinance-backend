FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /app/mojefinance-service
COPY . .
RUN mvn clean install -DskipTests

WORKDIR /app/mojefinance-application
COPY . .
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# We don't need to create folders or symlinks for the config file.
# We only need the folder for the CERTS if your code specifically
# looks for them at a hardcoded path.
RUN mkdir -p /app/resources/certs

# Copy the built jar
COPY --from=builder /app/mojefinance-application/target/mojefinance-application-1.0-SNAPSHOT.jar app.jar

EXPOSE 8081

# Tell Spring exactly where to find the secrets Render provides
ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.config.import=file:/etc/secrets/application-render.properties"]