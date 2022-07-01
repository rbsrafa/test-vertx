FROM openjdk:8-jre

ENTRYPOINT ["java", "-jar", "/usr/share/vertx/server.jar"]

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
ADD target/libs           /usr/share/vertx/libs
# Add the service itself
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/vertx/server.jar

EXPOSE 8585