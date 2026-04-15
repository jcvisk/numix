FROM eclipse-temurin:25-jre
ARG TOMCAT_VERSION=11.0.8
ENV CATALINA_HOME=/opt/tomcat
ENV PATH="${CATALINA_HOME}/bin:${PATH}"

RUN apt-get update && \
    apt-get install -y --no-install-recommends curl ca-certificates tar && \
    rm -rf /var/lib/apt/lists/* && \
    curl -fsSL "https://archive.apache.org/dist/tomcat/tomcat-11/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz" -o /tmp/tomcat.tar.gz && \
    mkdir -p /opt && \
    tar -xzf /tmp/tomcat.tar.gz -C /opt && \
    mv "/opt/apache-tomcat-${TOMCAT_VERSION}" "${CATALINA_HOME}" && \
    rm -f /tmp/tomcat.tar.gz

COPY docker/tomcat-entrypoint.sh /usr/local/bin/tomcat-entrypoint.sh
RUN chmod +x /usr/local/bin/tomcat-entrypoint.sh

EXPOSE 8080
CMD ["tomcat-entrypoint.sh"]
