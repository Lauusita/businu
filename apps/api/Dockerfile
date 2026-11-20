# Imagen de la API de BUSINÚ para AWS App Runner (o cualquier entorno con Docker).
#   docker build -t businu-api/prod .        (desde apps/api)
# Las variables de entorno (DB_URL, JWT_SECRET, AWS_S3_BUCKET, SPRING_MAIL_*, SUPERVISOR_INICIAL_*…)
# se configuran en App Runner, nunca dentro de la imagen.

# ---- Etapa 1: compilar con Java 21 ----
FROM eclipse-temurin:21-jdk AS compilacion
WORKDIR /app
# Primero solo lo necesario para descargar dependencias: si cambia el código, esta capa se reutiliza
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null
COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test

# ---- Etapa 2: imagen final, solo Java y la aplicación ----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 --no-create-home businu
COPY --from=compilacion /app/build/libs/*-SNAPSHOT.jar app.jar
USER businu
EXPOSE 3000
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Duser.timezone=UTC"
ENTRYPOINT ["java", "-jar", "app.jar"]
