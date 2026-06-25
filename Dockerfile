# 1. 빌드 단계 (Java 21 사용)
FROM eclipse-temurin:21-jdk-jammy AS build

ARG SENTRY_AUTH_TOKEN
ENV SENTRY_AUTH_TOKEN=$SENTRY_AUTH_TOKEN

COPY . .
# 테스트는 로컬에서 이미 통과했으니 배포 시에는 건너뜁니다.
RUN ./gradlew clean build -x test

# 2. 실행 단계
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 보안용 비루트(Non-root) 시스템 계정 생성 (UID/GID 1001)
RUN groupadd -g 1001 portracker && \
    useradd -r -u 1001 -g portracker portracker

# 빌드된 jar 파일을 app.jar로 복사
COPY --from=build /build/libs/*.jar app.jar

# SQLite DB 및 로그를 저장할 디렉토리 사전 생성 및 소유권 격리
RUN mkdir -p /app/db /app/logs && \
    chown -R portracker:portracker /app

# 일반 사용자로 컨테이너 샌드박스 보안 실행자 지정
USER portracker

# 포트 설정
EXPOSE 10000

# 메모리 최적화 (Render 무료 티어 512MB에 맞춤)
ENV JAVA_OPTS="-Xms256m -Xmx384m -XX:+UseSerialGC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$PORT -jar app.jar"]