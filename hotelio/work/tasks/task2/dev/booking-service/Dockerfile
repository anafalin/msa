# ---------- Build stage ----------
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

# Копируем только файлы сборки, чтобы закешировать слой зависимостей
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

# Делаем gradlew исполняемым (на случай, если в репо потерян +x)
RUN chmod +x gradlew

# Прогреваем кеш зависимостей (не валим сборку, если что-то пошло не так)
RUN ./gradlew --no-daemon dependencies || true

# Копируем исходники и собираем
COPY src ./src

RUN ./gradlew --no-daemon clean bootJar -x test

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN useradd --system --create-home --uid 1001 appuser \
    && chown -R appuser:appuser /app

USER appuser

COPY --chown=appuser:appuser --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]