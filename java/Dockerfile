FROM openjdk:17-jdk-slim

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .

RUN chmod +x ./gradlew

COPY src src

RUN ./gradlew bootJar --no-daemon

EXPOSE 8003

RUN mv build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]