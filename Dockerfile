#FROM openjdk:17-jdk
#ARG JAR_FILE=*.jar
#COPY ${JAR_FILE} application.jar
#ENTRYPOINT ["java", "-jar", "application.jar"]
FROM maven:3.8.4-openjdk-17 as builder

COPY src ./src
COPY pom.xml .

RUN mvn package

FROM openjdk:17-jdk

COPY --from=builder ../target/jira-bot-0.0.1-SNAPSHOT.jar application.jar

ENTRYPOINT ["java", "-jar", "application.jar"]
