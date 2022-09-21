FROM openjdk:11.0.1-jdk-slim
EXPOSE 8080
COPY run.sh /app/
COPY target/verify-service-*.jar /app/app.jar
WORKDIR /app
ENTRYPOINT ["bash", "run.sh"]
