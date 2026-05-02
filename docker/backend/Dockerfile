# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

RUN apk add --no-cache maven

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
# Добавляем флаг прямо в команду сборки
RUN mvn clean package -DskipTests -Dmaven.test.skip=true -Dmaven.compiler.parameters=true

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# ВАЖНО: здесь тоже нужен флаг, иначе JVM не поймет байт-код с превью-фичами
ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]