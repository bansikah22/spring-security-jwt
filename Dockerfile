# syntax=docker/dockerfile:1.7
# Copyright (c) 2026 Bansikah. Licensed under the terms in LICENSE.

ARG JAVA_VERSION=25.0.4_7
ARG MAVEN_VERSION=4.0.0-rc-6

FROM eclipse-temurin:${JAVA_VERSION}-jdk-noble AS build

ARG MAVEN_VERSION
WORKDIR /workspace

RUN apt-get update \
    && apt-get install --no-install-recommends --yes curl ca-certificates \
    && curl --fail --location --silent --show-error --output "apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
        "https://dlcdn.apache.org/maven/maven-4/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
    && curl --fail --location --silent --show-error --output "apache-maven-${MAVEN_VERSION}-bin.tar.gz.sha512" \
        "https://dlcdn.apache.org/maven/maven-4/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz.sha512" \
    && sha512sum --check "apache-maven-${MAVEN_VERSION}-bin.tar.gz.sha512" \
    && tar --extract --file="apache-maven-${MAVEN_VERSION}-bin.tar.gz" --directory=/opt \
    && ln --symbolic "/opt/apache-maven-${MAVEN_VERSION}/bin/mvn" /usr/local/bin/mvn \
    && rm "apache-maven-${MAVEN_VERSION}-bin.tar.gz" "apache-maven-${MAVEN_VERSION}-bin.tar.gz.sha512" \
    && rm --recursive --force /var/lib/apt/lists/*

# Copy the POM first so dependency downloads remain cached until dependencies change.
COPY pom.xml .
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline

COPY src src
RUN mvn --batch-mode --no-transfer-progress --offline package -DskipTests

FROM eclipse-temurin:${JAVA_VERSION}-jre-noble AS runtime

WORKDIR /app
RUN groupadd --system --gid 10001 secureportal \
    && useradd --system --uid 10001 --gid secureportal --no-create-home secureportal

COPY --from=build --chown=secureportal:secureportal /workspace/target/secure-portal-0.0.1-SNAPSHOT.jar app.jar

USER secureportal:secureportal
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/urandom", "-jar", "/app/app.jar"]