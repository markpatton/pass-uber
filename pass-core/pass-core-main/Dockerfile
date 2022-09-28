FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/pass-core-main.jar .
COPY entrypoint.sh .

ENTRYPOINT ["./entrypoint.sh"]
