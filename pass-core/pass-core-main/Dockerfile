FROM openjdk:11-jre-slim

RUN apt update \
    && apt install -y curl

WORKDIR /app

COPY target/pass-core-main.jar .
COPY entrypoint.sh .

ENTRYPOINT ["./entrypoint.sh"]
