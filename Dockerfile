FROM maven:3.8.4-openjdk-17 as build
WORKDIR /app
COPY src ./src
COPY pom.xml .
RUN mvn package

FROM openjdk:17-jdk as builder
WORKDIR /app
COPY --from=build /app/target/jira-bot-0.0.1-SNAPSHOT.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:17-jdk
WORKDIR /app
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]



#COPY --from=builder ../target/jira-bot-0.0.1-SNAPSHOT.jar application.jar
#ENTRYPOINT ["java", "-jar", "application.jar"]
