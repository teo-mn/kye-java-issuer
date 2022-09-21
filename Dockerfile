FROM openjdk:11.0.1-jdk-slim
COPY run.sh /app/
COPY target/verify-service-*.jar /app/app.jar
WORKDIR /app
ENTRYPOINT ["bash", "run.sh"]
