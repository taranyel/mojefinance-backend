FROM maven:3.9.8-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .

COPY mojefinance-application/pom.xml ./mojefinance-application/
COPY mojefinance-service/pom.xml ./mojefinance-service/
COPY mojefinance-service/api-model/pom.xml ./mojefinance-service/api-model/
COPY mojefinance-service/account-service/pom.xml ./mojefinance-service/account-service/
COPY mojefinance-service/bank-connection-service/pom.xml ./mojefinance-service/bank-connection-service/
COPY mojefinance-service/user-service/pom.xml ./mojefinance-service/user-service/

COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN mkdir -p /app/mojefinance-application/src/main/resources/certs

# Create a symlink from Render's secret path to your app's expected path
# Note: This works because Render mounts /etc/secrets at runtime
RUN ln -s /etc/secrets/ceska-sporitelna-cert.pem /app/mojefinance-application/src/main/resources/certs/ceska-sporitelna-cert.pem
RUN ln -s /etc/secrets/ceska-sporitelna-key.key /app/mojefinance-application/src/main/resources/certs/ceska-sporitelna-key.key

COPY --from=builder /app/mojefinance-application/target/mojefinance-application-1.0-SNAPSHOT.jar app.jar
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]