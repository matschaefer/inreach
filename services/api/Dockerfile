FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /build
COPY . .
RUN sh mvnw -B -ntp package -DskipTests
FROM eclipse-temurin:25-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* && useradd --system --uid 10001 inreach
WORKDIR /app
COPY --from=build /build/target/inreach-api-*.jar app.jar
USER inreach
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
