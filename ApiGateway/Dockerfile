FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 9000
# Simple delay so Config Server can come up before the gateway starts.
# Override with WAIT_FOR_CONFIG_SECONDS if you need longer/shorter waits.
ENV WAIT_FOR_CONFIG_SECONDS=10
ENTRYPOINT ["sh", "-c", "sleep ${WAIT_FOR_CONFIG_SECONDS:-10} && java -jar app.jar"]
