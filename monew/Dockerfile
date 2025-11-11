# 빌드 스테이지
FROM amazoncorretto:17 AS builder

WORKDIR /app

COPY gradlew ./gradlew
RUN chmod +x gradlew
COPY gradle ./gradle
COPY settings.gradle ./settings.gradle
COPY build.gradle ./build.gradle
RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew clean bootJar -x test --no-daemon

# 런타임 스테이지
FROM amazoncorretto:17

WORKDIR /app

# curl 설치
RUN yum update -y && yum install -y curl && yum clean all

ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=local \
    PROJECT_NAME=sb05-monew-team7 \
    PROJECT_VERSION=1.0.0

COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]