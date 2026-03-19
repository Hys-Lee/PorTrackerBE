# 1. 빌드 단계 (Java 21 사용)
FROM eclipse-temurin:21-jdk-jammy AS build
COPY . .
# 테스트는 로컬에서 이미 통과했으니 배포 시에는 건너뜁니다.
RUN ./gradlew clean build -x test

# 2. 실행 단계
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 빌드된 jar 파일을 app.jar로 복사
COPY --from=build /build/libs/*.jar app.jar

# SQLite DB 파일을 저장할 폴더 생성
RUN mkdir -p /app/db

# 포트 설정
EXPOSE 8080

# 메모리 최적화 (Render 무료 티어 512MB에 맞춤)
ENV JAVA_OPTS="-Xms256m -Xmx384m -XX:+UseSerialGC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]