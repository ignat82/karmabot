FROM maven:3.8.4-openjdk-17 as builder
WORKDIR /app
COPY src ./src
COPY pom.xml .
RUN mvn package

FROM openjdk:17-jdk as extractor
WORKDIR /app
COPY --from=builder /app/target/jira-bot-0.0.1-SNAPSHOT.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:17-jdk as launcher
WORKDIR /app
COPY --from=extractor /app/dependencies/ ./
COPY --from=extractor /app/spring-boot-loader/ ./
COPY --from=extractor /app/snapshot-dependencies/ ./
COPY --from=extractor /app/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

