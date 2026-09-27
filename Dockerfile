# CI에서 ./gradlew bootJar로 만든 jar를 그대로 담는 런타임 이미지
# 로컬에서 직접 빌드할 때: ./gradlew bootJar && docker build -t with-dog .
FROM eclipse-temurin:21-jre

WORKDIR /app
RUN useradd --system --uid 1001 spring && mkdir -p /app/uploads && chown spring /app/uploads
COPY build/libs/*-SNAPSHOT.jar app.jar

USER spring
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
