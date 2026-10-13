FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S turnopro && adduser -S -G turnopro turnopro
COPY --from=build /workspace/target/turnopro-api-1.0.0.jar /app/turnopro.jar
USER turnopro
ENV PORT=10000 JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC"
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/turnopro.jar"]
