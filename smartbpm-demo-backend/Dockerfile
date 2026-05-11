# Stage 1: Build con Maven y Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime con JRE 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/smartbpm-demo-backend.jar app.jar
EXPOSE 8081

# Ejecutamos con el perfil de AWS activo
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=aws"]
