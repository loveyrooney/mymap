# Stage 1: Build
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Copy gradle wrapper and config files first for layer caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (fail gracefully if it expects src)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code and build
COPY src src
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app

# 기본 시간대 설정 (선택 사항)
ENV TZ=Asia/Seoul

# Copy the built jar
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
