# Сборка
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
# Собираем строго под Java 21 без превью
RUN mvn clean package -DskipTests

# Запуск
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
# Если всё же нужно оставить превью-фичи, добавь флаг в ENTRYPOINT:
# ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]
ENTRYPOINT ["java", "-jar", "app.jar"]