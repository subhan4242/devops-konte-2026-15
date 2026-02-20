# TODO: This Dockerfile works but is not optimal for production!
# Entrypoint / CMD missing - application does not start on docker run!

FROM maven:3.9.5-eclipse-temurin-21

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

EXPOSE 8080