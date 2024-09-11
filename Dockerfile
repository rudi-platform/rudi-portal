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
ADD rudi-microservice/rudi-microservice-acl/rudi-microservice-acl-facade/target/rudi-microservice-acl-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-apigateway
ADD rudi-microservice/rudi-microservice-apigateway/rudi-microservice-apigateway-facade/target/rudi-microservice-apigateway-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-gateway
ADD rudi-microservice/rudi-microservice-gateway/rudi-microservice-gateway-facade/target/rudi-microservice-gateway-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-kalim
ADD rudi-microservice/rudi-microservice-kalim/rudi-microservice-kalim-facade/target/rudi-microservice-kalim-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-konsent
ADD rudi-microservice/rudi-microservice-konsent/rudi-microservice-konsent-facade/target/rudi-microservice-konsent-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-konsult
ADD rudi-microservice/rudi-microservice-konsult/rudi-microservice-konsult-facade/target/rudi-microservice-konsult-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-kos
ADD rudi-microservice/rudi-microservice-kos/rudi-microservice-kos-facade/target/rudi-microservice-kos-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-projekt
ADD rudi-microservice/rudi-microservice-projekt/rudi-microservice-projekt-facade/target/rudi-microservice-projekt-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-provider
ADD rudi-microservice/rudi-microservice-provider/rudi-microservice-provider-facade/target/rudi-microservice-provider-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-registry
ADD rudi-microservice/rudi-microservice-registry/rudi-microservice-registry-facade/target/rudi-microservice-registry-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-selfdata
ADD rudi-microservice/rudi-microservice-selfdata/rudi-microservice-selfdata-facade/target/rudi-microservice-selfdata-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-strukture
ADD rudi-microservice/rudi-microservice-strukture/rudi-microservice-strukture-facade/target/rudi-microservice-strukture-facade.jar /opt/rudi/microservice.jar

FROM rudi_base as rudi-microservice-template
ADD rudi-microservice/rudi-microservice-template/rudi-microservice-template-facade/target/rudi-microservice-template-facade.jar /opt/rudi/microservice.jar

