FROM eclipse-temurin:21-jdk AS compilacion
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null
COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test

# ---- Etapa 2: imagen final, solo con lo necesario para ejecutar ----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 businu
COPY --from=compilacion /app/build/libs/*-SNAPSHOT.jar app.jar
USER businu
EXPOSE 3000
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Duser.timezone=UTC"
ENTRYPOINT ["java", "-jar", "app.jar"]