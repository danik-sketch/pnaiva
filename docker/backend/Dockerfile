# Stage 1: Build
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Устанавливаем Maven вручную, так как готового образа с JDK 25 еще нет
RUN apk add --no-cache maven

# Копируем настройки и зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходники и собираем
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# JDK 25 требует флаг preview, если вы используете новые фичи
ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]