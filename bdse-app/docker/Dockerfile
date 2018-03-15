FROM openjdk:8-jdk-alpine
MAINTAINER Alexander Savin <alesavin@gmail.com>
ADD bdse-app-0.0.1-SNAPSHOT-exec.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]