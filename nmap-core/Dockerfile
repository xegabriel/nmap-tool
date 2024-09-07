FROM maven:3.8.6-openjdk-11 AS build
RUN mkdir -p /workspace
WORKDIR /workspace
COPY pom.xml /workspace/pom.xml
COPY src /workspace/src
RUN mvn -f /workspace/pom.xml clean install

FROM openjdk:11-jdk-slim
WORKDIR /app
COPY --from=build /workspace/target/nmap-core-0.0.1-SNAPSHOT.jar /data/nmap-core-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/data/nmap-core-0.0.1-SNAPSHOT.jar"]
