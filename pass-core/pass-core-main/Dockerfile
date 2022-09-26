FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/pass-core-main.jar .

ENTRYPOINT ["java","-jar","pass-core-main.jar"]
