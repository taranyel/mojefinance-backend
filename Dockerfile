FROM maven:3.9.8-eclipse-temurin-21 AS builder

WORKDIR /app

COPY mojefinance-application/pom.xml ./mojefinance-application/
COPY mojefinance-service/pom.xml ./mojefinance-service/
COPY mojefinance-service/api-model/pom.xml ./mojefinance-service/api-model/
COPY mojefinance-service/account-service/pom.xml ./mojefinance-service/account-service/
COPY mojefinance-service/bank-connection-service/pom.xml ./mojefinance-service/bank-connection-service/
COPY mojefinance-service/user-service/pom.xml ./mojefinance-service/user-service/

COPY . .

WORKDIR /app/mojefinance-service
RUN mvn clean install

WORKDIR /app/mojefinance-application
RUN mvn clean install

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /app/mojefinance-application/target/mojefinance-application-1.0-SNAPSHOT.jar app.jar
EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.config.import=file:/etc/secrets/application-render.yaml"]