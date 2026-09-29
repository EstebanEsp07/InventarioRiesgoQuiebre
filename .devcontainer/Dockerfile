FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

# Node 20 para el frontend React
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y --no-install-recommends nodejs postgresql-client python3 python3-pip python3-venv \
    && rm -rf /var/lib/apt/lists/*

# Maven
ARG MAVEN_VERSION=3.9.9
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    | tar -xz -C /opt && ln -s /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/local/bin/mvn
