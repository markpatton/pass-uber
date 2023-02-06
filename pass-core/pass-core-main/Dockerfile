FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/pass-core-main.jar .
COPY entrypoint.sh .

RUN apt update \
    && apt install -y curl \
    && chmod +x entrypoint.sh \
    && rm -rf /var/lib/apt/lists/*

ENTRYPOINT ["./entrypoint.sh"]
