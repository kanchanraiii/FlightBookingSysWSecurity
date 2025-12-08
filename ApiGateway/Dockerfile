FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 9000
ENV WAIT_FOR_CONFIG_SECONDS=30
ENTRYPOINT ["sh", "-c", "sleep ${WAIT_FOR_CONFIG_SECONDS:-10} && java -jar app.jar"]
