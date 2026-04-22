# Etapa 1: Compilación
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copiar solo el pom.xml para cachear dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el resto del código fuente
COPY src ./src

# Compilar y empaquetar (genera el JAR)
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiar el JAR generado desde la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Puerto que expone la aplicación (Spring Boot por defecto 8080)
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]