# ---------- build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- execução ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Planos gratuitos têm ~512 MB de RAM e pouca CPU: limita a memória do JVM e usa um coletor leve.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:+UseSerialGC -Xss512k -XX:ReservedCodeCacheSize=48m -XX:TieredStopAtLevel=1"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
