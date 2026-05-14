FROM maven:3.9.9-eclipse-temurin-21 AS build

ARG MODULE
WORKDIR /workspace

COPY . .
RUN ./mvnw -pl ${MODULE} -am -DskipTests package

FROM eclipse-temurin:21-jre

ARG MODULE
ARG PORT=8080
ENV SERVER_PORT=${PORT}

WORKDIR /app
COPY --from=build /workspace/${MODULE}/target/*.jar /app/app.jar

EXPOSE ${PORT}

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
