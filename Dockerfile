# Step 1: Build JAR
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# DEBUG: List target folder to confirm jar path
RUN ls -R /app/target

# Step 2: Run the JAR with Java 17
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/cab-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
