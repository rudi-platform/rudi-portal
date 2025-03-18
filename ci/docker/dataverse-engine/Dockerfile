# Copyright 2023 Forschungszentrum Jülich GmbH
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
#
################################################################################################################
#
# THIS FILE IS TO BE USED WITH MAVEN DOCKER BUILD:
# mvn -Pct clean package
#
################################################################################################################
#
# Some commands used are inspired by https://github.com/payara/Payara/tree/master/appserver/extras/docker-images.
# Most parts origin from older versions of https://github.com/gdcc/dataverse-kubernetes.
#
# We are not using upstream Payara images because:
#  - Their image is less optimised for production usage and Dataverse by design choices
#  - We provide multi-arch images
#  - We provide some tweaks for development and monitoring
#

# Make the Java base image and version configurable (useful for trying newer Java versions and flavors)
ARG BASE_IMAGE="gdcc/base:6.6-noble-p6.2024.6-j17"
FROM $BASE_IMAGE

# Make Payara use the "ct" profile for MicroProfile Config. Will switch various defaults for the application
# setup in META-INF/microprofile-config.properties.
# See also https://download.eclipse.org/microprofile/microprofile-config-3.0/microprofile-config-spec-3.0.html#configprofile
ENV MP_CONFIG_PROFILE=ct

# Workaround to configure upload directories by default to useful place until we can have variable lookups in
# defaults for glassfish-web.xml and other places.
ENV DATAVERSE_FILES_UPLOADS="${STORAGE_DIR}/uploads"
ENV DATAVERSE_FILES_DOCROOT="${STORAGE_DIR}/docroot"

#USER root
#RUN apt-get update && apt-get upgrade -y

# Copy app and deps from assembly in proper layers
COPY --chown=payara:payara maven/deps ${DEPLOY_DIR}/dataverse/WEB-INF/lib/
COPY --chown=payara:payara maven/app ${DEPLOY_DIR}/dataverse/
COPY --chown=payara:payara maven/supplements ${DEPLOY_DIR}/dataverse/supplements/
COPY --chown=payara:payara maven/scripts ${SCRIPT_DIR}/
RUN chmod +x "${SCRIPT_DIR}"/*

# Create symlinks for jHove
RUN ln -s "${DEPLOY_DIR}/dataverse/supplements/jhove.conf" "${PAYARA_DIR}/glassfish/domains/${DOMAIN_NAME}/config/jhove.conf" && \
    ln -s "${DEPLOY_DIR}/dataverse/supplements/jhoveConfig.xsd" "${PAYARA_DIR}/glassfish/domains/${DOMAIN_NAME}/config/jhoveConfig.xsd" && \
    sed -i "${PAYARA_DIR}/glassfish/domains/${DOMAIN_NAME}/config/jhove.conf" -e "s:/usr/local/payara./glassfish/domains/domain1:${PAYARA_DIR}/glassfish/domains/${DOMAIN_NAME}:g"

LABEL org.opencontainers.image.created="2025-01-22T15:58:05+0000" \
      org.opencontainers.image.authors="Research Data Management at FZJ <forschungsdaten@fz-juelich.de>" \
      org.opencontainers.image.url="https://guides.dataverse.org/en/latest/container/" \
      org.opencontainers.image.documentation="https://guides.dataverse.org/en/latest/container/" \
      org.opencontainers.image.source="https://github.com/IQSS/dataverse" \
      org.opencontainers.image.version="6.5" \
      org.opencontainers.image.revision="69ebed23" \
      org.opencontainers.image.vendor="Global Dataverse Community Consortium" \
      org.opencontainers.image.licenses="Apache-2.0" \
      org.opencontainers.image.title="Dataverse Application Image" \
      org.opencontainers.image.description="This container image provides the research data repository software Dataverse in a box." \
      org.dataverse.deps.postgresql.version="15" \
      org.dataverse.deps.solr.version="9.4.1"
