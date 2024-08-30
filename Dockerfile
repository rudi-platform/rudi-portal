ARG FROM_TAG=11.0.9.1-jre-buster
FROM openjdk:${FROM_TAG} as rudi_base
USER root
LABEL org.opencontainers.image.authors=rudi@rennes-metropole.fr
ENV TZ=Europe/Paris

RUN apt-get update && apt-get -y install ghostscript \
    && rm -rf /var/lib/apt/lists/*
RUN mkdir -p /etc/rudi/config && mkdir -p /opt/rudi/data && mkdir -p /tmp/jetty && chmod -R a+rwx /tmp

CMD [ "sh", "-c", "exec java -Djava.io.tmpdir=/tmp/jetty \
      -Drudi.datadir=/opt/rudi/data \
      -Drudi.config=/etc/rudi/config \
      -Dlogging.config=/etc/rudi/config/log4j2.xml \
      -Dspring.config.additional-location=file:/etc/rudi/config/ \
      ${JAVA_OPTIONS}                                   \
      -Xmx${XMX:-1G} -Xms${XMX:-1G}                      \
      -jar /opt/rudi/microservice.jar" ]


FROM rudi_base as rudi-microservice-acl
ADD target/rudi-microservice/rudi-microservice-acl/rudi-microservice-acl-facade/rudi-microservice-acl-facade.jar /opt/rudi/microservice.jar
COPY "acl/relai-smtp-ext.rennesmetropole.fr.port26.cer" "$JAVA_HOME/lib/security"
RUN keytool -import -file "$JAVA_HOME/lib/security/relai-smtp-ext.rennesmetropole.fr.port26.cer" -alias "relai-smtp-ext.rennesmetropole.fr.port26" -cacerts -noprompt -storepass "changeit"

FROM rudi_base as rudi-microservice-apigateway
ADD target/rudi-microservice/rudi-microservice-apigateway/rudi-microservice-apigateway-facade/rudi-microservice-apigateway-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-gateway
ADD target/rudi-microservice/rudi-microservice-gateway/rudi-microservice-gateway-facade/rudi-microservice-gateway-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-kalim
ADD target/rudi-microservice/rudi-microservice-kalim/rudi-microservice-kalim-facade/rudi-microservice-kalim-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-konsent
ADD target/rudi-microservice/rudi-microservice-konsent/rudi-microservice-konsent-facade/rudi-microservice-konsent-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-konsult
ADD target/rudi-microservice/rudi-microservice-konsult/rudi-microservice-konsult-facade/rudi-microservice-konsult-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-kos
ADD target/rudi-microservice/rudi-microservice-kos/rudi-microservice-kos-facade/rudi-microservice-kos-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-projekt
ADD target/rudi-microservice/rudi-microservice-projekt/rudi-microservice-projekt-facade/rudi-microservice-projekt-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-provider
ADD target/rudi-microservice/rudi-microservice-provider/rudi-microservice-provider-facade/rudi-microservice-provider-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-registry
ADD target/rudi-microservice/rudi-microservice-registry/rudi-microservice-registry-facade/rudi-microservice-registry-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-selfdata
ADD target/rudi-microservice/rudi-microservice-selfdata/rudi-microservice-selfdata-facade/rudi-microservice-selfdata-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-strukture
ADD target/rudi-microservice/rudi-microservice-strukture/rudi-microservice-strukture-facade/rudi-microservice-strukture-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-template
ADD target/rudi-microservice/rudi-microservice-template/rudi-microservice-template-facade/rudi-microservice-template-facade.jar /opt/rudi/microservice.jar

