# =============================================================================
# Stage 1 — Builder
# =============================================================================
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY gradle.properties .
COPY settings.gradle .

RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

COPY src src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon -x test

# =============================================================================
# Stage 2 — Runtime
# =============================================================================
FROM eclipse-temurin:25-jre-alpine AS runtime

WORKDIR /app

RUN addgroup --system desafio-itau \
    && adduser --system --ingroup desafio-itau desafio-itau

COPY --from=builder \
     --chown=desafio-itau:desafio-itau \
     /app/build/libs/desafio-itau-0.0.1-SNAPSHOT.jar app.jar

USER desafio-itau

EXPOSE 8080

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]