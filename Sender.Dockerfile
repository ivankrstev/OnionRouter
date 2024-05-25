FROM maven:3.8.4-openjdk-17 as build
WORKDIR /build-dependencies/
COPY pom.xml ./
RUN mvn dependency:go-offline
COPY src/ ./src
RUN mvn clean package

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app/
COPY --from=build /build-dependencies/target/* ./libs/
COPY ./src/main/java/org/onionrouter/Sender.java ./org/onionrouter/
RUN javac -cp "./libs/*" ./org/onionrouter/Sender.java
ENTRYPOINT ["java", "-cp", "libs/*", "org/onionrouter/Sender"]
