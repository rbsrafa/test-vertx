FROM openjdk:8-jre

ENTRYPOINT ["java", "-jar", "/usr/share/vertx/client.jar"]

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
ADD target/libs           /usr/share/vertx/libs
# Add the service itself
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/vertx/client.jar

RUN apt-get update \
  && DEBIAN_FRONTEND=noninteractive apt-get install -y \
    net-tools \
  && apt-get clean \
  && apt-get install -y watch \
  && rm -rf /var/lib/apt/lists/*

EXPOSE 8586