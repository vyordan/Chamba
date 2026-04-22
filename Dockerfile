# Etapa 1: Compilación con Maven y JDK 17
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copiar el archivo de configuración de dependencias primero (para cachear)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final más liviana con Amazon Corretto 17 (Alpine)
FROM amazoncorretto:17-alpine

WORKDIR /app

# Copiar el JAR generado desde la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Puerto de la aplicación
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]